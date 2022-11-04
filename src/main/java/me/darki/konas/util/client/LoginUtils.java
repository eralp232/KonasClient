package me.darki.konas.util.client;

import com.google.common.base.Charsets;
import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import com.mojang.util.UUIDTypeAdapter;
import me.darki.konas.gui.altmanager.AltSummary;
import me.darki.konas.mixin.mixins.IMinecraft;
import me.darki.konas.util.network.APIUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

import java.util.AbstractMap;
import java.util.UUID;

public class LoginUtils {

    public static final YggdrasilAuthenticationService loginService;
    private static final YggdrasilUserAuthentication userService;
    private static final YggdrasilMinecraftSessionService sessionService;

    static {
        loginService = new YggdrasilAuthenticationService(Minecraft.getMinecraft().getProxy(), UUID.randomUUID().toString());
        userService = (YggdrasilUserAuthentication) loginService.createUserAuthentication(Agent.MINECRAFT);
        sessionService = (YggdrasilMinecraftSessionService) loginService.createMinecraftSessionService();
    }

    public static boolean loginServiceOnline() {
        return loginService.getClientToken() != null;
    }

    public static boolean login(AltSummary summary, String user, String password) {
        LoginUtils.userService.setUsername(user);
        LoginUtils.userService.setPassword(password);
        try {
            LoginUtils.userService.logIn();
        } catch (AuthenticationException e) {
            return false;
        }
        String username = LoginUtils.userService.getSelectedProfile().getName();
        String uuid = UUIDTypeAdapter.fromUUID(LoginUtils.userService.getSelectedProfile().getId());
        String access = LoginUtils.userService.getAuthenticatedToken();
        String type = LoginUtils.userService.getUserType().getName();
        summary.setUuid(uuid);
        summary.setToken(access);
        summary.setName(username);
        ((IMinecraft) Minecraft.getMinecraft()).setSession(new Session(username, uuid, access, type));
        LoginUtils.userService.logOut();
        return true;
    }

    public static boolean loginMicrosoft(AltSummary altSummary, String email, String password) {
        APIUtils.LoginCombo combo = APIUtils.sendMicrosoftLoginRequest(email, password);
        if(combo == null) {
            return false;
        }
        altSummary.setToken(combo.getToken());
        altSummary.setName(combo.getName());
        altSummary.setUuid(combo.getUuid());
        ((IMinecraft) Minecraft.getMinecraft()).setSession(new Session(combo.getName(), combo.getUuid(), combo.getToken(), "mojang"));
        return true;
    }


    public static boolean loginOffline(String username) throws IllegalArgumentException {
        UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(Charsets.UTF_8));
        ((IMinecraft) Minecraft.getMinecraft()).setSession(new Session(username, uuid.toString(), "invalid", "legacy"));
        return true;
    }

    public static boolean isOffline() {
        return Minecraft.getMinecraft().getSession().getProfile().getId().equals(UUID.nameUUIDFromBytes(("OfflinePlayer:" + Minecraft.getMinecraft().getSession().getUsername()).getBytes(Charsets.UTF_8)));
    }

    public static boolean sessionValid() {
        try {
            GameProfile gp = Minecraft.getMinecraft().getSession().getProfile();
            String token = Minecraft.getMinecraft().getSession().getToken();
            String id = UUID.randomUUID().toString();

            LoginUtils.sessionService.joinServer(gp, token, id);
            if (LoginUtils.sessionService.hasJoinedServer(gp, id, null).isComplete()) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
}

#version 120

uniform sampler2D DiffuseSamper;
uniform vec2 TexelSize;
uniform vec4 Color;
uniform vec4 Fill;
uniform int SampleRadius;
uniform bool RenderOutline;
uniform bool OutlineFade;

void main()
{
    vec4 centerCol = texture2D(DiffuseSamper, gl_TexCoord[0].st);

    if(centerCol.a != 0.0F)
    {
        gl_FragColor = vec4(Fill.r, Fill.g, Fill.b, Fill.a);
        return;
    }
    float closest = SampleRadius * 1.0F;
    for(int xo = -SampleRadius; xo <= SampleRadius; xo++)
    {
        for(int yo = -SampleRadius; yo <= SampleRadius; yo++)
        {
            vec4 currCol = texture2D(DiffuseSamper, gl_TexCoord[0].st + vec2(xo * TexelSize.x, yo * TexelSize.y));
            if(currCol.r != 0.0F || currCol.g != 0.0F || currCol.b != 0.0F || currCol.a != 0.0F)
            {
                float currentDist = sqrt(xo * xo + yo * yo);
                if(currentDist < closest)
                {
                    closest = currentDist;
                }
            }
        }
    }
    if (RenderOutline) {
        float fade = max(0, ((SampleRadius * 1.0F) - (closest - 1)) / (SampleRadius * 1.0F));
        if (OutlineFade) {
            float colorFade = max(0, fade - 1F);
            gl_FragColor = vec4(Color.r - colorFade, Color.g - colorFade, Color.b - colorFade, fade);
        } else {
            if (fade > 0.5F) {
                gl_FragColor = vec4(Color.r, Color.g, Color.b, Color.a);
            } else {
                gl_FragColor = vec4(0F, 0F, 0F, 0F);
            }
        }
    } else {
        gl_FragColor = vec4(0F, 0F, 0F, 0F);
    }
}
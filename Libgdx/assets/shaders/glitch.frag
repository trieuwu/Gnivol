#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform float u_time;
uniform float u_intensity; // Độ nặng của bệnh (0.0 đến 1.0)

// Hàm random tạo nhiễu
float rand(vec2 co){
    return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
}

void main() {
    vec2 uv = v_texCoords;

    // 1. Móp méo Pixel (Cắt xé hình ảnh theo chiều ngang)
    float glitchBand = step(0.9, sin(uv.y * 15.0 + u_time * 10.0));
    float offset = (rand(vec2(u_time, uv.y)) - 0.5) * 0.15 * u_intensity * glitchBand;
    uv.x += offset;

    // 2. Chromatic Aberration (Phân tách viền Xanh - Đỏ)
    float shift = 0.04 * u_intensity;
    vec4 colorR = texture2D(u_texture, vec2(uv.x + shift, uv.y));
    vec4 colorG = texture2D(u_texture, uv);
    vec4 colorB = texture2D(u_texture, vec2(uv.x - shift, uv.y));

    vec4 finalColor = vec4(colorR.r, colorG.g, colorB.b, colorG.a);

    // 3. Đảo ngược bảng màu (Âm bản) chớp nhoáng
    if (rand(vec2(u_time, u_time)) > 0.85 && u_intensity > 0.5) {
        finalColor.rgb = vec3(1.0) - finalColor.rgb;
    }

    gl_FragColor = finalColor * v_color;
}

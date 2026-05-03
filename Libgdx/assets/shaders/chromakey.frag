#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;
uniform vec3 u_keyColor;     // Màu cần xóa, vd vec3(0.0, 1.0, 0.0) cho xanh lá thuần
uniform float u_threshold;   // Khoảng cách màu coi là "khớp" key — càng nhỏ càng strict (0.0 - 1.0)
uniform float u_smoothing;   // Độ mềm viền (anti-aliasing) — 0.05 - 0.2

void main() {
    vec4 color = texture2D(u_texture, v_texCoords) * v_color;

    // Khoảng cách Euclid trong RGB space giữa pixel và key color
    float diff = distance(color.rgb, u_keyColor);

    // smoothstep: pixel càng giống key → alpha càng thấp; càng khác → alpha = 1
    float alpha = smoothstep(u_threshold, u_threshold + u_smoothing, diff);

    gl_FragColor = vec4(color.rgb, color.a * alpha);
}

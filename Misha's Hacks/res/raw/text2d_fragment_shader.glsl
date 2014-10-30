
precision mediump float; 

uniform sampler2D  u_TextureUnit; 
varying vec2       v_TextureCoordinates;      	   								

varying vec4       v_Color; 


void main() { 
	gl_FragColor = texture2D(u_TextureUnit, v_TextureCoordinates) * v_Color; 
	gl_FragColor.rgb *= v_Color.a; 
};  

This will contain a part of Psuedo Random Generator for our `02201 Agile Hardware Design` course at `The Technical University of Denmark`. This repo is publicly available for use under the `MIT License`.

**The repo, for now, will contain 2 algorithms (Salsa20 and ChaCha (Bernstien)), and there tests(test vector along with their expected results for each). **

The source code for both can be found under `\src\main\scala\` .  
Their tests are present under `\src\test\scala\` .   


##SALSA20:##

The Port List for salsa20 contains input enciphered text and output ciphered text(and its implementation), but with they are commented out, as they are not required by the scope of the project. 
Salsa work on little endian where as chisels Unit works on Big endian. The output is tested on 3 distant test vectors whose output is calculated and then the algorithm is tested against it.


-**Port List**  

	- Inputs

		- in_start              ` input valid - wight = 1 `
		- in_key		        ` input key from Sha256 - wight = 512 `
		- in_nonce		        ` input nonce - wight = 64 `
		- in_counter   		    ` input ctr - wight = 64 `
		- in_unciphered_text   	`commented out - wight = 512 `    		
		
	- Outputs
		
		- out_ready		        ` output valid - wight = 1 `
		- out_Decoding_key	    ` output key for decoding - wight = 512 `
		- out_ciphered_text  	`commented out - wight = 512 `  



##ChaCha (Bernstien):## 

The Port List for Chacha contains input enciphered text and output ciphered text(and its implementation), but with they are commented out, as they are not required by the scope of the project. 

Chacha also works on little endian where as chisels Unit works on Big endian. The output is tested on 3 distant test vectors from IETF(Internet Engineering Task Force). 


-**Port List**  

	- Inputs

		- in_start              ` input valid - wight = 1 `
		- in_key		        ` input key from Sha256 - wight = 512 `
		- in_nonce		        ` input nonce - wight = 64 `
		- in_counter   		    ` input ctr - wight = 64 `
		- in_unciphered_text   	`commented out - wight = 512 `    		
		
	- Outputs
		
		- out_ready		        ` output valid - wight = 1 `
		- out_Decoding_key	    ` output key for decoding - wight = 512 `
		- out_ciphered_text  	`commented out - wight = 512 `  



`* The difference between the two is in the internal working of the cipher, whereas the output logic and port list remains the same. `  



##Top fsm##


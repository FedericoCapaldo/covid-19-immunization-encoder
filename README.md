# SIMPLE COVID QR CODE GENERATOR

This generator is able to take your personal information and generate a QR code as a proof of vaccine. Inspired by: `https://dx.dragan.ba/covid-certificate-encoding/` "Let's make a Covid Certificate [ENCODING]". 

This process goes through the following steps: `JSON > CBOR Binary > COSE Signed (Mocked step that cannot be replicated) > Zlib Compression > Base45 > QR Code`

The generator is not able to have a verified COSE signature by design, so scanning the QR code produced with an iPhone will allow you to save a valid immunization record, but it will also say that the record is not verified (and therefore cannot be added to Wallet, only to Health App). Besides this limitation, which again cannot be overcome by design (you can read up more about `digital signature check`), it generates a valid QR code with immunization information.


TO RUN:

1. Download this repo (or alternatively copy and paste Main.java to your local Main file and then add libraries through Maven)

2. Edit the `json` variable to match your information.

3. Compile and run code. This will print on the screen a long string in the format of `HC1:...`.

4. Generate QR code with the string from step 3 here `https://www.qr-code-generator.com/solutions/text-qr-code/` and done, you can scan it (probably you will need to enlarge the code to be able to scan it).

Note: Android QR code scanning doesn't seem abilitated to scan this code. iPhone will scan and understand it correctly. 

If you are curious you can see the content of `HC1:` encoded string use this decoder `https://dencode.com/en/string/base45`


## Note: This was just a fun project to understand the technologies behind Covid Immunization App. Get Vaccinated!   
import COSE.*;
import com.upokecenter.cbor.CBORObject;
import nl.minvws.encoding.Base45;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

/* * * * *
SHAPE OF INPUT JSON FOR '1 of 2' dose

{
  "v" : [
    {
    "ci" : "URN:UVCI:01:GB:1235393923269VQV7QY1E#M",
    "co" : "GB",
    "dn" : 2,
    "dt" : "2022-10-20",
    "is" : "NHS Digital",
    "ma" : "ORG-100030214",
    "mp" : "EU/1/20/1528",
    "sd" : 1,
    "tg" : "840239001",
    "vp" : "1118349006"
  }
],
  "dob" : "1980-10-01",
  "nam" : {
    "fn" : "Doe",
    "gn" : "John",
    "fnt" : "DOE",
    "gnt" : "JOHN"
  },
  "ver" : "1.3.0"
}

* * * * */


public class Main {
    public static void main(String[] args) {

        // see comment above to create this json
        String json = "{\n" +
                "  \"v\" : [\n" +
                "    {\n" +
                "    \"ci\" : \"URN:UVCI:01:GB:1235393923269VQV7QY1E#M\",\n" +
                "    \"co\" : \"GB\",\n" +
                "    \"dn\" : 2,\n" +
                "    \"dt\" : \"2022-10-20\",\n" +
                "    \"is\" : \"NHS Digital\",\n" +
                "    \"ma\" : \"ORG-100030214\",\n" +
                "    \"mp\" : \"EU/1/20/1528\",\n" +
                "    \"sd\" : 1,\n" +
                "    \"tg\" : \"840239001\",\n" +
                "    \"vp\" : \"1118349006\"\n" +
                "  }\n" +
                "],\n" +
                "  \"dob\" : \"1980-10-01\",\n" +
                "  \"nam\" : {\n" +
                "    \"fn\" : \"Doe\",\n" +
                "    \"gn\" : \"John\",\n" +
                "    \"fnt\" : \"DOE\",\n" +
                "    \"gnt\" : \"JOHN\"\n" +
                "  },\n" +
                "  \"ver\" : \"1.3.0\"\n" +
                "}";

        String country = "GB";
        long issuedAtSec = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() / 1000L;
        long expirationSec = LocalDateTime.now().plusYears(1).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() / 1000L;

        // CBOR
        CBORObject map = CBORObject.NewMap();
        CBORObject hcertVersion = CBORObject.NewMap();
        CBORObject hcert = CBORObject.FromJSONString(json);
        hcertVersion.set(CBORObject.FromObject(1), hcert);
        map.set(CBORObject.FromObject(1), CBORObject.FromObject(country));
        map.set(CBORObject.FromObject(6), CBORObject.FromObject(issuedAtSec));
        map.set(CBORObject.FromObject(4), CBORObject.FromObject(expirationSec));
        map.set(CBORObject.FromObject(-260), hcertVersion);

        byte[] cbor = map.EncodeToBytes();

        try {
            // COSE
            OneKey privateKey = OneKey.generateKey(AlgorithmID.ECDSA_256);
            byte[] kid = UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8);

            Sign1Message msg = new Sign1Message();
            msg.addAttribute(HeaderKeys.Algorithm, privateKey.get(KeyKeys.Algorithm), Attribute.PROTECTED);
            msg.addAttribute(HeaderKeys.KID, CBORObject.FromObject(kid), Attribute.PROTECTED);
            msg.SetContent(cbor);
            msg.sign(privateKey);

            byte[] cose = msg.EncodeToBytes();



            // COMPRESS
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            try (CompressorOutputStream deflateOut = new CompressorStreamFactory()
                    .createCompressorOutputStream(CompressorStreamFactory.DEFLATE,
                            stream)) {
                deflateOut.write(cose);
            }
            byte[] zip = stream.toByteArray();


            // BASE 45 + Prefix
            String base45 = Base45.getEncoder().encodeToString(zip);
            String hc1 = "HC1:" + base45;

            // TAKE this code, turn it into a QR code (https://www.qr-code-generator.com/solutions/text-qr-code/) and scan it with iPhone. Done.
            System.out.println(hc1);

        } catch (Exception e) {

           e.printStackTrace();
        }


    }
}

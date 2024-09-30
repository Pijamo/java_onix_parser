import com.tectonica.jonix.Jonix;
import com.tectonica.jonix.common.codelist.*;
import com.tectonica.jonix.common.struct.JonixLanguage;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class JonixTest {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please provide the path to an ONIX file or directory as an argument.");
            return;
        }

        File onixSource = new File(args[0]);

        Jonix.source(onixSource)
                .onSourceStart(src -> {
                    System.out.printf(">> Opening %s (ONIX release %s)%n", src.sourceName(), src.onixRelease());
                    src.header().map(Jonix::toBaseHeader)
                            .ifPresent(header -> System.out.printf(">> Sent from: %s%n", header.senderName));
                })
                .onSourceEnd(src -> {
                    System.out.printf("<< Processed %d products (total %d) %n",
                            src.productCount(), src.productGlobalCount());
                })
                .stream()
                .map(Jonix::toBaseProduct)
                .forEach(product -> {

                    //Onix Product Reference Number
                    String ref = product.info.recordReference;

                    //Product ISBN
                    String isbn13 = product.info.findProductId(ProductIdentifierTypes.ISBN_13);

                    //Product Languages List
                    List<JonixLanguage> languageList = product.description.languages;

                    //Product Language Codes
                    List<String> languageCodes = languageList.stream()
                            .map(language -> language.languageCode.getCode())
                            .collect(Collectors.toList());

                    //Product Title
                    String title = product.titles.findTitleText(TitleTypes.Distinctive_title_book);

                    //Product Pub Name
                    String pubName = product.publishers.getMainPublisher();

                    //Product Pub Date
                    String pubDate = product.publishingDetails.publicationDate;

                    //Product Contributors
                    List<String> contributors = product.contributors.stream()
                            .map(contributor -> {
                                String name;
                                name = contributor.getDisplayName();

                                String roles = contributor.contributorRoles.stream()
                                        .map(role -> role.name())
                                        .collect(Collectors.joining(", "));
                                return String.format("%s (%s)", name, roles);
                            })
                            .collect(Collectors.toList());


                    System.out.println("ref                 = " + ref);
                    System.out.println("isbn13              = " + isbn13);
                    System.out.println("title               = " + title);
                    System.out.println("language            = " + languageCodes);
                    System.out.println("contributors        = " + contributors);
                    System.out.println("publisher           = " + pubName);
                    System.out.println("publication date    = " + pubDate);
                    System.out.println("----------------------------------------------------------");
                });

    }
}
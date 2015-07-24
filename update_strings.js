var Localize = require("localize-with-spreadsheet");
var transformer = Localize.fromGoogleSpreadsheet("1I_Da7eqbZpbTHCRS9pU0GpcCnAi3OjGDllPrfjUFvDQ", '*');
transformer.setKeyCol('key');

transformer.save("app/src/main/res/values/strings.xml", { valueCol: "en", format: "android" });
transformer.save("app/src/main/res/values-en/strings.xml", { valueCol: "en", format: "android" });
transformer.save("app/src/main/res/values-fr/strings.xml", { valueCol: "fr", format: "android" });

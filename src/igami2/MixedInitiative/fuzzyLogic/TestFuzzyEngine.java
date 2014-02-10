package igami2.MixedInitiative.fuzzyLogic;

//Testing Fuzzy engine by Edward S. Sazonov (esazonov@usa.com) Copyright (C) 2000
// author: Meghna Babbar, Aug 2004

/**
 * This class evaluates rules given by string variables.
 * Evaluation is performed either on a single rule or a block of rules.
 */
public class TestFuzzyEngine
{


        /**
         * Constructor.
         * Engine initialization is performed here.
         */
        public TestFuzzyEngine()
        {
              //1. Create Lingustic variables and define membership functions
              LinguisticVariable temperature = new LinguisticVariable("temperature");
              temperature.add("cold",55,55,110,165);
              temperature.add("cool",110,165,165,220);
              temperature.add("normal",165,220,220,275);
              temperature.add("warm",220,275,275,330);
              temperature.add("hot",275,330,385,385);

              LinguisticVariable pressure = new LinguisticVariable("pressure");
              pressure.add("weak",0,0,10,65);
              pressure.add("low",10,65,65,120);
              pressure.add("ok",65,120,120,175);
              pressure.add("strong",120,175,175,230);
              pressure.add("high",175,230,285,285);

              LinguisticVariable throttleAction = new LinguisticVariable("throttleAction");
              throttleAction.add("NL",-84,-84,-60,-36);
              throttleAction.add("NM",-60,-36,-36,-12);
              throttleAction.add("NS",-36,-12,-12,12);
              throttleAction.add("ZR",-12,0,0,12);
              throttleAction.add("PS",-12,12,12,36);
              throttleAction.add("PM",12,36,36,60);
              throttleAction.add("PL",36,60,84,84);

              // testing
              //temperature.setInputValue(130);
              //pressure.setInputValue(100);

              //2. Create a fuzzy engine
              FuzzyEngine fuzzyEngine = new FuzzyEngine();

              //3. Register all LVs
              fuzzyEngine.register(temperature);
              fuzzyEngine.register(pressure);
              fuzzyEngine.register(throttleAction);

              //4. Create a block of rules
              FuzzyBlockOfRules fuzzyBlockOfRules = new FuzzyBlockOfRules(" if temperature is cool and pressure is weak then throttleAction is PL \n if temperature is cool and pressure is low then throttleAction is PM \n if temperature is cool and pressure is ok then throttleAction is ZR \n if temperature is cool and pressure is strong then throttleAction is NM ");

              //5. Register the block
              fuzzyEngine.register(fuzzyBlockOfRules);

              //6. Parse the rules
              try {
                  fuzzyBlockOfRules.parseBlock();
              }
              catch (Exception e){
                  e.printStackTrace();
                  System.exit(1);
              }

              // testing
              temperature.setInputValue(135);
              pressure.setInputValue(85);

              //7. Perform the evaluation
              try {
                  fuzzyBlockOfRules.evaluateBlock(); //- faster execution
                  fuzzyBlockOfRules.evaluateBlockText(); //- slower execution, returns a String with evaluation results for every fuzzy expression
              }
              catch (Exception e){
                  e.printStackTrace();
                  System.exit(1);
              }


              //8. Obtain the result(s)
              double result = 0.0;
              try {
                  result = throttleAction.defuzzify();
              }
              catch (Exception e){
                  e.printStackTrace();
                  System.exit(1);
              }

              System.out.println("throttleAction fuzzy result : " + result);
              System.out.println("temperature input value : " + temperature.getInputValue());
              System.out.println("pressure input value : " + pressure.getInputValue());

        }

        public static void main(String[] args) {

        TestFuzzyEngine testFuzzyEngine = new TestFuzzyEngine();

        }
}

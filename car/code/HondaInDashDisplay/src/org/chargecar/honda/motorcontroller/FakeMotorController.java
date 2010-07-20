package org.chargecar.honda.motorcontroller;

import org.chargecar.honda.FakeSerialDevice;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class FakeMotorController extends FakeSerialDevice
   {
   public FakeMotorController()
      {
      super(DATA);
      }

   private static final String DATA = "::SRPM    0\f" +
                                      "::SRPM    0\f" +
                                      "::SRPM    0\f" +
                                      "::SRPM    0:SRPM    0\f" +
                                      "::SRPM    0\f" +
                                      "::SRPM    0\f" +
                                      "::SRPM   13\f" +
                                      "(:SRPM   98\f" +
                                      "+:SRPM  202\f" +
                                      "::SRPM  308\f" +
                                      "1:SRPM  414\f" +
                                      ";:SRPM  514\f" +
                                      "::SRPM  602\f" +
                                      ">:SRPM  682\f" +
                                      "6:SRPM  762\f" +
                                      "9:SRPM  837\f" +
                                      "6:SRPM  904\f" +
                                      "7:SRPM  969\f" +
                                      "<:SRPM 1031\f" +
                                      "):SRPM 1086\f" +
                                      "%:SRPM 1140\f" +
                                      ".:SRPM 1196\f" +
                                      "%:SRPM 1249\f" +
                                      "$:SRPM 1300\f" +
                                      "(:SRPM 1353\f" +
                                      ".:SRPM 1401\f" +
                                      ".:SRPM 1448\f" +
                                      "#:SRPM 1487\f" +
                                      " :SRPM 1524\f" +
                                      "(:SRPM 1554\f" +
                                      "/:SRPM 1552\f" +
                                      "):SRPM 1472 ::SRPM 1346 ::SRPM 1197 4:SRPM 1057 9:SRPM  919 +:SRPM  792 &:SRPM  681 %:SRPM  591 ':SRPM  515 +:SRPM  458 #:SRPM  407 ):SRPM  361 .:SRPM  323 (:SRPM  290 !:SRPM  261 /:SRPM  234 /:SRPM  206 .:SRPM  183  :SRPM  160 -:SRPM  139 !:SRPM  122 +:SRPM  108 #:SRPM   94 7:SRPM   77 ::SRPM   65 9:SRPM   49 7:SRPM   34 =:SRPM   18 3:SRPM    6 ,:SRPM    0 *:SRPM    0 *:SRPM    0 *:SRPM    0 *:SRPM    0 *:SRPM    0 *:SRPM    0 *:SRPM    0\f" +
                                      "::SRPM    0\f" +
                                      "::SRPM    0\f" +
                                      "::SRPM    0\f" +
                                      "::SRPM    0\f" +
                                      "::SRPM    0\f" +
                                      "::SRPM    0\f" +
                                      "::SRPM    0\f" +
                                      "::SRPM    0\f" +
                                      "::SRPM    0\f" +
                                      "::SRPM   -6\f" +
                                      "1:SRPM -108\f" +
                                      ">:SRPM -260\f" +
                                      "3:SRPM -412\f" +
                                      "0:SRPM -546\f" +
                                      "0:SRPM -672\f" +
                                      "4:SRPM -789\f" +
                                      "1:SRPM -890\f" +
                                      "6:SRPM -988\f" +
                                      ">:SRPM-1078\f" +
                                      "):SRPM-1165\f" +
                                      "$:SRPM-1247\f" +
                                      "':SRPM-1331\f" +
                                      "':SRPM-1418\f" +
                                      "+:SRPM-1499\f" +
                                      "\":SRPM-1536\f" +
                                      "&:SRPM-1424\f" +
                                      "$:SRPM-1292\f" +
                                      "/:SRPM-1142\f" +
                                      "!:SRPM-1002\f" +
                                      "$:SRPM -861\f" +
                                      "8:SRPM -740\f" +
                                      "4:SRPM -636\f" +
                                      "4:SRPM -551\f" +
                                      "6:SRPM -486\f" +
                                      "=:SRPM -430\f" +
                                      "0:SRPM -379\f" +
                                      "::SRPM -337\f" +
                                      "0:SRPM -302\f" +
                                      "6:SRPM -270\f" +
                                      "2:SRPM -240\f" +
                                      "1:SRPM -210\f" +
                                      "4:SRPM -186\f" +
                                      "8:SRPM -165\f" +
                                      "5:SRPM -143\f" +
                                      "1:SRPM -126\f" +
                                      "2:SRPM -111\f" +
                                      "6:SRPM  -99\f" +
                                      "':SRPM  -80\f" +
                                      "/:SRPM  -62\f" +
                                      "#:SRPM  -46\f" +
                                      "%:SRPM  -29\f" +
                                      ",:SRPM  -14\f" +
                                      "\":SRPM    0\f" +
                                      "::SRPM    0\f" +
                                      "::SRPM    0\f" +
                                      "::SRPM    0\f" +
                                      "::SRPM    0\f" +
                                      "::SRPM    0\f" +
                                      "::SRPM    0\f" +
                                      "::SCode 29\f" +
                                      "c:SCode 29\f" +
                                      "c:SCode 29\f" +
                                      "c:SCode 29\f" +
                                      "c:SCode 29\f" +
                                      "c:SCode 29\f" +
                                      "c:SCode 29\f" +
                                      "c:SCode 29\f" +
                                      "c:SCode 29\f" +
                                      "c:SCode 29\f" +
                                      "c:SCode 29\f" +
                                      "c:SCode 29\f" +
                                      "c:SCode 29\f" +
                                      "c:SCode 29\f" +
                                      "c:SCode 29\f" +
                                      "c:SCode 29\f" +
                                      "c:SCode 29\f" +
                                      "::SRPM    0\f" +
                                      "::SRPM    0\f" +
                                      "::SRPM    0\f";
   }

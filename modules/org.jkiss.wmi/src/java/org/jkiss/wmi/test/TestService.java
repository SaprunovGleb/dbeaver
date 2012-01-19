/*
 * Copyright (c) 2012, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.wmi.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jkiss.wmi.service.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * WMI Service tester
 */
public class TestService {

    final static Log log = LogFactory.getLog(TestService.class);

    private WMIService service;
    private boolean finished = false;

    public TestService()
    {
    }

    public static void main(String[] args)
    {
        new TestService()
            .test();
    }

    void test()
    {
        log.info("Start service");
        WMIService.initializeThread();
        try {
            //service.connect("bq", "aelita", "jurgen", "CityMan78&", null, "\\root\\cimv2");
            service = WMIService.connect(log, null, "localhost", null, null, null, "\\root");
            final long curTime = System.currentTimeMillis();

/*
            WMIObject[] result = service.executeQuery(
                "select * from Win32_NTLogEvent where LogFile='System'", false);
            System.out.println("Exec time: " + (System.currentTimeMillis() - curTime) + "ms");

            for (WMIObject object : result) {
                printObject(object);
            }
*/


            WMIObjectSink objectExplorerSink = new WMIObjectSink() {
                private int totalObjects = 0;

                public void indicate(WMIObject[] objects) {
                    totalObjects += objects.length;
                    for (WMIObject object : objects) {
                        try {
                            examineObject(object);
                            //object.release();
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.println("Recieved " + objects.length + " (" + totalObjects + ") objects");
                }

                public void setStatus(WMIObjectSinkStatus status, int progress, String param, WMIObject errorObject) {
                    System.out.println("Status: " + status + "; " + Integer.toHexString(progress) + "; " + param);
                    if (errorObject != null) {
                        printObject(errorObject);
                    }
                    System.out.println("Total objects: " + totalObjects);
                    System.out.println("Exec time: " + (System.currentTimeMillis() - curTime) + "ms");
                    finished = true;
                }
            };
/*
            service.executeQuery(
                "select * from Win32_NTLogEvent",
                objectExplorerSink,
                WMIConstants.WBEM_FLAG_SEND_STATUS
            );
            try {
                while (!finished) {
                    Thread.sleep(100);
                }
            }
            catch (InterruptedException e) {
                // do nothing
            }
*/
            final WMIService nsService = service.openNamespace("cimv2");

            ObjectCollectorSink classesSink = new ObjectCollectorSink();
            nsService.enumClasses(null, classesSink, 0);
            classesSink.waitForFinish();
            for (WMIObject classDesc : classesSink.objectList) {
                final Collection<WMIObjectMethod> methods = classDesc.getMethods(WMIConstants.WBEM_FLAG_ALWAYS);
                if (methods != null) {

                }
            }

            ObjectCollectorSink objectCollectorSink = new ObjectCollectorSink();
/*
            service.executeQuery(
                //"select * from Win32_NTLogEvent",
                "select * from Win32_Group",
                objectCollectorSink,
                WMIConstants.WBEM_FLAG_SEND_STATUS
            );
*/
            nsService.enumInstances("Win32_Group", objectCollectorSink, WMIConstants.WBEM_FLAG_SEND_STATUS);
            objectCollectorSink.waitForFinish();

            for (WMIObject nsDesc : objectCollectorSink.objectList) {
                System.out.println(nsDesc.getValue("Name"));
                final Collection<WMIQualifier> qfList = nsDesc.getQualifiers();
                if (qfList != null) {

                }

//                final Object nsName = nsDesc.getValue("Name");
//                final WMIService nsService = service.openNamespace(nsName.toString());
//
//                ObjectCollectorSink classCollectorSink = new ObjectCollectorSink();
//                nsService.enumClasses(null, classCollectorSink, WMIConstants.WBEM_FLAG_SEND_STATUS | WMIConstants.WBEM_FLAG_SHALLOW);
//                classCollectorSink.waitForFinish();
//                nsService.close();
            }

        } catch (WMIException e) {
            e.printStackTrace();
        }
        finally {
            service.close();
            WMIService.unInitializeThread();
        }

        System.gc();
        System.out.println("DONE");
    }

    private static void printObject(WMIObject object)
    {
        try {
            System.out.println("====== " + object.getObjectText());
        } catch (WMIException e) {
            e.printStackTrace();
        }
    }

    private static void examineObject(WMIObject object) throws WMIException
    {
        final String objectText = object.getObjectText();
        //final Object name = object.getValue("Name");

        for (WMIObjectAttribute prop : object.getAttributes(WMIConstants.WBEM_FLAG_ALWAYS)) {
            Object propValue = prop.getValue();
            if (propValue instanceof Object[]) {
                //System.out.print("\t" + prop.getName() + "= { ");
                Object[] array = (Object[])propValue;
                for (int i = 0; i < array.length; i++) {
                    //if (i > 0) System.out.print(", ");
                    //System.out.print("'" + array[i] + "'");
                }
                //System.out.println(" }");
            } else if (propValue instanceof byte[]) {
                //System.out.println("\t" + prop.getName() + "= { byte array } " + ((byte[])propValue).length);
            } else {
                //System.out.println("\t" + prop.getName() + "=" + propValue);
            }
        }
    }

    private class ObjectCollectorSink implements WMIObjectSink
    {
        private final List<WMIObject> objectList;
        private boolean finished = false;

        public ObjectCollectorSink()
        {
            this.objectList = new ArrayList<WMIObject>();
        }

        public void indicate(WMIObject[] objects)
        {
            Collections.addAll(objectList, objects);
        }

        public void setStatus(WMIObjectSinkStatus status, int result, String param, WMIObject errorObject)
        {
            if (status == WMIObjectSinkStatus.complete) {
                finished = true;
            }
        }

        public void waitForFinish()
        {
            try {
                while (!finished) {
                    Thread.sleep(100);
                }
                //service.cancelAsyncOperation(wmiObjectSink);
            }
            catch (InterruptedException e) {
                // do nothing
            }
        }
    }
}

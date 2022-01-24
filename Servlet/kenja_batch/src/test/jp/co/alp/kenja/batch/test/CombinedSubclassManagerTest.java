// kanji=漢字
/*
 * $Id: CombinedSubclassManagerTest.java 74557 2020-05-27 05:13:43Z maeshiro $
 *
 * 作成日: 2006/12/21 13:43:41 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.test;

import junit.framework.TestCase;

public class CombinedSubclassManagerTest extends TestCase {

//    private static final String PROPSNAME = "AccumulateSummaryBatch.properties";
//    private static final Log log = LogFactory.getLog(CombinedSubclassManagerTest.class);
//
//    private AccumulateOptions _options;
//    private DbConnection _dbcon;
//    private ControlMaster _cm;
//    final MyEnum.Category _category = new MyEnum.Category(); // アプリケーションで唯一のnew MyEnum.Category()
//    
//    private CombinedSubClassManager _manager; 
//
//    protected void setUp() throws Exception {
//        super.setUp();
//        
//        final Properties properties = new Properties();
//        try {
//            final File propFile = new File(PROPSNAME);
//            log.debug(" property file = " + propFile.getAbsolutePath());
//            properties.load(new FileInputStream(propFile));
//        } catch (final FileNotFoundException e) {
//            log.error("FileNotFoundException", e);
//        } catch (final IOException e) {
//            log.error("IOException", e);
//        }
//        
//        // -夜間バッチの引数
//        String[] args = StringUtils.split("dbname=R1214TE dbhost=jaguar staffcd=00999999 baseday=1 date=2006-07-30", " ");
//        
//        _options = new AccumulateOptions(args, properties);
//        
//        _dbcon = null;
//        try {
//            _dbcon = connection(_options.getKenjaParameter());
//            _cm = getControlMaster(_options.getKenjaParameter(), _dbcon);
//        } catch (final Exception e) {
//            // CHECKSTYLE:ON
//            log.fatal("== invoke failure", e);
//        }
//        DaoSubClass.getInstance().load(_dbcon.getROConnection(), _cm);
//        DaoCombinedSubClass.getInstance().load(_dbcon.getROConnection(), _cm);
//        
//        _manager = CombinedSubClassManager.getInstance();
//    }
//    
//    /**
//     * CombinedSubclassManager#getAttendSubClassesメソッドテスト
//     * @throws SQLException
//     */
//    public void testInstanceMethodIsCombinedSubClass() throws SQLException {
//        final Collection combinedSubclasses = _manager.getCombinedSubClasses();
//        for (Iterator it = combinedSubclasses.iterator(); it.hasNext();) {
//            SubClass combined = (SubClass) it.next();
//            Collection attendSubclasses = _manager.getAttendSubClasses(combined);
//            
//            for (Iterator it2 = attendSubclasses.iterator(); it2.hasNext();) {
//                SubClass attend = (SubClass) it2.next();
//                assertEquals(true, _manager.isCombinedSubClass(combined, attend));
//            }
//        }
//    }
//
//    /**
//     * CombinedSubclassManager#getCombinedSubClassesメソッドテスト
//     * @throws SQLException
//     */
//    public void testInstanceMethodGetCombinedSubClasses() throws SQLException {
//        final Collection combinedSubclasses = _manager.getCombinedSubClasses();
//        for (Iterator it = combinedSubclasses.iterator(); it.hasNext();) {
//            SubClass combined = (SubClass) it.next();
//            Collection attendSubclasses = _manager.getAttendSubClasses(combined);
//            
//            for (Iterator it2 = attendSubclasses.iterator(); it2.hasNext();) {
//                SubClass attend = (SubClass) it2.next();
//                
//                final Collection attendsCombinedSubclasses = _manager.getCombinedSubClasses(attend);
//                assertEquals(true, attendsCombinedSubclasses.contains(combined));
//            }
//        }
//    }
//
//    protected void tearDown() throws Exception {
//        if (null != _dbcon) {
//            _dbcon.closeQuietly();
//            log.debug("DBCP Closed");
//        }
//    }
//
//    private DbConnection connection(
//            final KenjaParameters kenjaParams
//    ) throws Exception {
//        final DbConnection dbcon;
//        try {
//            dbcon = DbConnectionImpl.create(kenjaParams);
//            if (null == dbcon) {
//                throw new Exception("DB接続情報を作成できない");
//            }
//            return dbcon;
//        } catch (final SQLException e) {
//            log.fatal("DB接続情報を作成中に例外", e);
//            throw e;
//        }
//    }
//    
//    private ControlMaster getControlMaster(
//            final KenjaParameters kenjaParameter,
//            final DbConnection dbcon
//    ) throws SQLException {
//        final DaoSemester daoSemes = new DaoSemester(_options.getDate());
//        daoSemes.load(dbcon.getROConnection());
//
//        return new ControlMaster(
//                _category,
//                daoSemes.getYear(),
//                daoSemes.getSemester(),
//                _options.getDate(),
//                ControlMaster.DISPLAY_SUBCLASS
//        );
//    }
} // CombineSubclassTest

// eof

// kanji=����
/*
 * $Id: KnjRecSchoolingDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2008/08/15 15:46:35 - JST
 * �쐬��: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.sql.SQLException;

import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.batch.withus.icass.migration.table_icass.SeitoSchoolingJisseki;

/**
 * REC_SCHOOLING_RATE_DAT �����B
 * @author takaesu
 * @version $Id: KnjRecSchoolingDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjRecSchoolingDat extends AbstractKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjRecSchoolingDat.class);

    private final static String[] YEAR_TABLE = {
            "2005",
            "2006",
            "2007",
            "2008",
    };

    /** 01:�ʊw��, 02:�T�|�[�g�Z��, 03:�l��, 04:��g�搶, 05:�Ȗڗ��C��, 06:���Ґ�, 07:�g�b�v�A�X���[�g��, 08:CP�R�[�X�� */
    private final static String[] STUDENT_DIV = {
        "01",
        "02",
        "03",
        "04",
        "05",
        "06",
        "07",
        "08",
    };
    final KnjRecSchoolingRateDat _knjRateDat = new KnjRecSchoolingRateDat();

    public KnjRecSchoolingDat() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "�ʐM�X�N�[�����O����"; }

    void migrate() throws SQLException {
        _knjRateDat.init(_db2, _param);

        for (int i = 0; i < YEAR_TABLE.length; i++) {
            // �u��ҘA�Ԃŕ������ď�������
            final String nendo = YEAR_TABLE[i];
            int renbanSize = 1000; // 1��̏����ŏ������ގu��ҘA�Ԃ̃T�C�Y
            for(int ren=0; ren<20000; ren+=renbanSize) { 
                int begin = ren+1;
                int end = ren+renbanSize;
                
                // ICASS�Ǎ���
                final MultiMap data = loadIcass(nendo, begin, end);
                log.debug("�� " + nendo + "�N�x �u��ҘA��="+(begin)+"�`"+(end)+" :�f�[�^����=" + data.size());
    
                // ICASS-->Kenja
                save(data);
                log.warn("seito_schooling_jisseki �̃f�[�^��S�āu�W���X�N�[�����O�v�Ƃ��āAREC_SCHOOLING_DAT �ɕۑ������B");

            }

            // ���ȃR�[�h���Ƃɕ������ď�������
            for(int classcd = 1; classcd < 20; classcd+=1) {
                // Kenja-->Kenja
                log.warn("REC_SCHOOLING_DAT ����� REC_SCHOOLING_RATE_DAT ���쐬���B");
                _knjRateDat.migrate(nendo, classcd);
            }
        }
    }

    private void save(final MultiMap data) throws SQLException {
        for (final Iterator it = data.keySet().iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            final Collection ssj���� = (Collection) data.get(student);
            saveKnj(student, ssj����);
        }
        _db2.commit();
    }

    private void saveKnj(final Student student, final Collection ssj����) throws SQLException {
        int totalCount = 0;
        final String sql = "INSERT INTO rec_schooling_dat VALUES(?,?,?,?,?,?,?,?,?,?,?,current timestamp)";

        for (final Iterator it = ssj����.iterator(); it.hasNext();) {
            final SeitoSchoolingJisseki ssj = (SeitoSchoolingJisseki) it.next();
            final Object[] array = ssj.toRecSchoolingDat(student.getNewSeq());
            final int insertCount = _runner.update(_db2.conn, sql, array);
            if (1 != insertCount) {
                throw new IllegalStateException("INSERT������1���ȊO!:" + insertCount);
            }
            totalCount += insertCount;
        }
    }

    private MultiMap loadIcass(final String nendo,int begin,int end) throws SQLException {
        final MultiMap rtn = new MultiHashMap();

        final String sql;
        sql = "SELECT"
            + "  t2.seito_no AS schregno,"
            + "  t1.*"
            + " FROM"
            + "  seito_schooling_jisseki t1"
            + "  INNER JOIN seito t2 ON t1.shigansha_renban=t2.shigansha_renban"
            + "  INNER JOIN schreg_regd_dat t3 ON t2.seito_no=t3.schregno"
            + " WHERE"
            + "  t3.semester='1' AND"
            + "  t1.nendo_code=t3.year AND"
            + "  t3.student_div='02'"  // TODO: Out of memory �ɑΉ�����!
//            + "  AND t2.seito_no like '052%'"
            + " AND t3.year='" + nendo + "'"
            + " AND int(t1.shigansha_renban) between "+ begin+ " and "+ end+" ";
            ;
        log.debug("sql=" + sql);

        final List result = _runner.mapListQuery(sql);

        // ���ʂ̏���
        for (final Iterator it = result.iterator(); it.hasNext();) {
            final Map map = (Map) it.next();

            final String schregno = (String) map.get("schregno");
            final Student student = getStudent(rtn.keySet(), schregno);
            final SeitoSchoolingJisseki ssj = new SeitoSchoolingJisseki(_param, map);
            rtn.put(student, ssj);
        }
        return rtn;
    }

    private Student getStudent(final Set students, String schregno) {
        for (final Iterator it = students.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            if (schregno.equals(student._schregno)) {
                return student;
            }
        }
        return new Student(schregno);
    }

    private class Student {
        private final String _schregno;
        /**
         * ��(REC_SCHOOLING_DAT.SEQ)�B
         */
        private int seq = 1;

        Student(final String schregno) {
            _schregno = schregno;
        }

        int getNewSeq() {
            return seq++;
        }
    }
}
// eof

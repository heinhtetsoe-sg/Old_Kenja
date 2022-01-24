package jp.co.alp.kenja.batch.otr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import jp.co.alp.kenja.batch.otr.domain.KenjaDateImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ���ގ��f�[�^
 * @author maesiro
 * @version $Id: RoomEnterExitData.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class RoomEnterExitData implements Comparable {

    private static Log log = LogFactory.getLog(RoomEnterExitData.class);

    private static final String VALID_CARD_DIV = "1";

    /** �f�[�^�敪 */
    private final String _dataDiv;
    /** �ō��N���� */
    private final KenjaDateImpl _date;
    /** ���� */
    private final BatchTime _time;
    /** �J�[�h��� */
    private final String _cardDiv;
    /** �J�敪 */
    private final String _openCloseDiv;
    /** ���ގ��敪 */
    private final String _enterExitDiv;
    /** �l�R�[�h(�ݐДԍ�) */
    private final String _schregno;
    /** �Q�[�gNo. */
    private final String _gateNo;

    /** �搶�� */
    private final boolean _isTeacher;

    /**
     * �R���X�g���N�^
     * @param schregno �w�Дԍ�
     * @param date ����
     * @param time ����
     * @param dataDiv �f�[�^�敪
     * @param cardDiv �J�[�h�敪
     * @param openCloseDiv �J�敪
     * @param enterExitDiv ���ގ��敪
     * @param gateNo �Q�[�g�ԍ�
     * @param isTeacher �搶��
     */
    public RoomEnterExitData(
            final String schregno,
            final KenjaDateImpl date,
            final BatchTime time,
            final String dataDiv,
            final String cardDiv,
            final String openCloseDiv,
            final String enterExitDiv,
            final String gateNo,
            final boolean isTeacher) {
        _schregno = schregno;
        _date = date;
        _time = time;
        _dataDiv = dataDiv;
        _cardDiv = cardDiv;
        _openCloseDiv = openCloseDiv;
        _enterExitDiv = enterExitDiv;
        _gateNo = gateNo;

        _isTeacher = isTeacher;
    }

    /**
     * ������Ԃ�
     * @return ����
     */
    public KenjaDateImpl getDate() {
        return _date;
    }

    /**
     * �搶��
     * @return �搶�Ȃ�true, �����łȂ����false
     */
    public boolean isStaff() {
        return _isTeacher;
    }

    /**
     * �ݐДԍ���Ԃ�
     * @return �ݐДԍ�
     */
    public String getSchregno() {
        return _schregno;
    }

    /**
     * ���Ԃ�Ԃ�
     * @return ����
     */
    public BatchTime getTime() {
        return _time;
    }

    /**
     * �Q�[�gNo.��Ԃ�
     * @return �Q�[�gNo.
     */
    public String getGateno() {
        return _gateNo;
    }

    /**
     * �w�肳�ꂽ�ʒu�̃g�[�N���𓾂�
     *
     * ----- �f�[�^�t�H�[�}�b�g -----
     * dataDiv �f�[�^�敪 2byte
     * year �N            4byte
     * month ��           2byte
     * date ��            2byte
     * hour ����          2byte
     * minute ����        2byte
     * cardDiv �J�[�h�敪 1byte
     * aux �\��           1byte
     * openCloseDiv �J�敪   1byte
     * enterExitDiv ���ގ��敪 1byte
     * schregno �w�Дԍ� 10byte
     * gateNo �Q�[�g�ԍ�  4byte
     *                 �v32byte
     *                 
     * @param nth �g�[�N���̈ʒu
     * @param dataLine 1�s�̃f�[�^
     * @return �g�[�N��
     */
    private static String getToken(final int nth, final String dataLine) {
        final int[] sizes = new int[]{2, 4, 2, 2, 2, 2, 1, 1, 1, 1, 10, 4};
        final int[] beginIndex = new int[sizes.length];

        int ti = 0;
        for (int i = 0; i < sizes.length; i++) {
            beginIndex[i] = ti;
            ti += sizes[i];
        }
        return dataLine.substring(beginIndex[nth], beginIndex[nth] + sizes[nth]);
    }

    /**
     * ���ގ��f�[�^�쐬
     * @param enterExitString �ΑӃt�@�C���̃f�[�^�̂P�s
     * @return ���ގ��f�[�^
     */
    public static RoomEnterExitData create(final String enterExitString) {
        int p = 0;
        try {
            final String dataDiv = getToken(p++, enterExitString);
            final int year = Integer.parseInt(getToken(p++, enterExitString));
            final int month = Integer.parseInt(getToken(p++, enterExitString));
            final int date = Integer.parseInt(getToken(p++, enterExitString));
            final int hour = Integer.parseInt(getToken(p++, enterExitString));
            final int minute = Integer.parseInt(getToken(p++, enterExitString));
            final String cardDiv = getToken(p++, enterExitString);
            if (!VALID_CARD_DIV.equals(cardDiv)) {
                throw new IllegalArgumentException("cardDiv = " + cardDiv);
            }
            p++; // �\���f�[�^
            final String openCloseDiv = getToken(p++, enterExitString);
            final String enterExitDiv = getToken(p++, enterExitString);
            final String studentCodeField = getToken(p++, enterExitString);
            final String gateNo = getToken(p++, enterExitString);

            final KenjaDateImpl kenjaDate = KenjaDateImpl.getInstance(year, month, date);
            final BatchTime time = BatchTime.create(hour, minute);
            final boolean isTeacher = studentCodeField.charAt(3) == '2'; 

            RoomEnterExitData data =  new RoomEnterExitData(
                    "20" + studentCodeField.substring(4),
                    kenjaDate, 
                    time,
                    dataDiv,
                    cardDiv,
                    openCloseDiv,
                    enterExitDiv, 
                    gateNo,
                    isTeacher // �ݐДԍ��t�B�[���h��2���ڂ�'2'�Ȃ�搶
            ); 
                        
            return data;
        } catch (final IllegalArgumentException e) {
            log.error("�����ȃf�[�^ " + enterExitString + " , " + p + " th field " + e.getMessage());
        }
        return null;
    }

    /**
     * �ΑӃt�@�C����ǂݍ��݋ΑӃf�[�^�̃��X�g��Ԃ�
     * @param file �ΑӃt�@�C��
     * @return �ΑӃf�[�^�̃��X�g
     * @throws IOException IO��O
     */
    public static List load(final File file) throws IOException {
        final List dataList = new LinkedList();
        final BufferedReader br = new BufferedReader(new FileReader(file));
        int dataCount = 0;
        log.debug("OTR�f�[�^�t�@�C���ǂݍ��݊J�n�B");
        for (String line = "";; dataCount++) {
            line = br.readLine();
            if (line == null) {
                break;
            }
            final RoomEnterExitData data = RoomEnterExitData.create(line);
            if (null == data) {
                continue;
            }
            log.debug(data);
            dataList.add(data);
        }
        log.debug("OTR�f�[�^�t�@�C���ǂݍ��ݏI���B �f�[�^�� = " + dataCount + ", �L���f�[�^�� = " + dataList.size());
        br.close();

        return dataList;
    }

    /**
     * �����Ŕ�r����B����������Ȃ玞���Ŕ�r����B
     * {@inheritDoc}
     */
    public int compareTo(final Object o) {
        if (o instanceof RoomEnterExitData) {
            return 0;
        }
        final RoomEnterExitData other = (RoomEnterExitData) o;
        int rtn = _date.compareTo(other._date);
        if (rtn == 0) {
            rtn = _time.compareTo(other._time);
        }
        return rtn;
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return _schregno.hashCode() + _date.hashCode() + _time.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        final StringBuffer stb = new StringBuffer();
        stb.append("�ݐДԍ�=").append(_schregno);
        stb.append(", ���t=").append(_date.toString());
        stb.append(", ����=").append(_time.toString());
        stb.append(", �Q�[�gNo.=").append(_gateNo);
        stb.append(isStaff() ? "(�搶)" : "");
        return stb.toString();
    }
}

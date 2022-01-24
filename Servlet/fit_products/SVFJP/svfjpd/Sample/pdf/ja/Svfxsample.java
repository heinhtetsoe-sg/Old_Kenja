/**
 * @(#)Svfxsample.java  200710/26
 *
 * SVF for Java Print
 *
 * Sample Program
 *
 */

import jp.co.fit.vfreport.*;

public class Svfxsample {

  public static void main(String[] args) {
    int ret;
    Vrw32 svf = new Vrw32();
    ret = svf.VrInit();
    ret = svf.VrSetPrinter("", "PDF");
    ret = svf.VrSetSpoolFileName2("Svfxsample.pdf");
    ret = svf.VrSetForm("svfxhachusho.xml", 4);
    ret = svf.VrsOut("���s�N����", "2007/10/26 00000");
    ret = svf.VrsOut("�����ԍ�", "1000476");
    ret = svf.VrsOut("�d���於", "�E�C���O�A�[�N �e�N�m���W�[�Y�������");
    ret = svf.VrsOut("�d����X�֔ԍ�", "��111-1111");
    ret = svf.VrsOut("�d����Z��", "�����s�����恠��������1-1-1");
    ret = svf.VrsOut("�d����d�b�ԍ�", "03-1234-5678");
    ret = svf.VrsOut("�x������", "�[��������������");
    ret = svf.VrsOut("�[�i�ꏊ", "��222-2222 �����s�����恠����2-2-2");
    ret = svf.VrsOut("�������הԍ�", "1000522");
    ret = svf.VrsOut("���i��", "SVFX-Designer");
    ret = svf.VrsOut("�P��", "700000.00");
    ret = svf.VrsOut("����", "2.00");
    ret = svf.VrsOut("���z", "1400000.00");
    ret = svf.VrsOut("�d���搻�i�ԍ�", "SVF01");
    ret = svf.VrEndRecord();
    ret = svf.VrsOut("���s�N����", "2007/10/26 00000");
    ret = svf.VrsOut("�����ԍ�", "1000476");
    ret = svf.VrsOut("�d���於", "�E�C���O�A�[�N �e�N�m���W�[�Y�������");
    ret = svf.VrsOut("�d����X�֔ԍ�", "��111-1111");
    ret = svf.VrsOut("�d����Z��", "�����s�����恠��������1-1-1");
    ret = svf.VrsOut("�d����d�b�ԍ�", "03-1234-5678");
    ret = svf.VrsOut("�x������", "�[��������������");
    ret = svf.VrsOut("�[�i�ꏊ", "��222-2222 �����s�����恠����2-2-2");
    ret = svf.VrsOut("�������הԍ�", "1000523");
    ret = svf.VrsOut("���i��", "SVF for JavaPrint");
    ret = svf.VrsOut("�P��", "600000.00");
    ret = svf.VrsOut("����", "3.00");
    ret = svf.VrsOut("���z", "1800000.00");
    ret = svf.VrsOut("�d���搻�i�ԍ�", "SVF02");
    ret = svf.VrEndRecord();
    ret = svf.VrsOut("���s�N����", "2007/10/26 00000");
    ret = svf.VrsOut("�����ԍ�", "1000476");
    ret = svf.VrsOut("�d���於", "�E�C���O�A�[�N �e�N�m���W�[�Y�������");
    ret = svf.VrsOut("�d����X�֔ԍ�", "��111-1111");
    ret = svf.VrsOut("�d����Z��", "�����s�����恠��������1-1-1");
    ret = svf.VrsOut("�d����d�b�ԍ�", "03-1234-5678");
    ret = svf.VrsOut("�x������", "�[��������������");
    ret = svf.VrsOut("�[�i�ꏊ", "��222-2222 �����s�����恠����2-2-2");
    ret = svf.VrsOut("�������הԍ�", "1000524");
    ret = svf.VrsOut("���i��", "SVF for PDF");
    ret = svf.VrsOut("�P��", "800000.00");
    ret = svf.VrsOut("����", "1.00");
    ret = svf.VrsOut("���z", "800000.00");
    ret = svf.VrsOut("�d���搻�i�ԍ�", "SVF03");
    ret = svf.VrEndRecord();
    ret = svf.VrPrint();
    ret = svf.VrQuit();
  }
}

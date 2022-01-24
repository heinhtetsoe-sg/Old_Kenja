<?php

require_once('for_php7.php');

class knjl302eModel extends Model {
    var $cmd;
    var $examyear;
    var $applicantdiv;
    var $testdiv;
    var $testscoreprint;

    function knjl302eModel(){}

    function init() {

        $this->cmd            = VARS::request("cmd");

        $this->examyear       = CTRL_YEAR + 1;
        $this->applicantdiv   = VARS::post("APPLICANTDIV");
        $this->testscoreprint = VARS::post("TESTSOCRE_PRINT");

        $this->testdiv        = VARS::post("TESTDIV");
    }

    /********************/
    /*  �b�r�u�o�͏���  */
    /********************/
    function getDownloadModel() {
        $db = Query::dbCheckOut();
        $header = array();

        $header["ENTEXAMYEAR"]      = "�����N�x";
        $header["EXAMNO"]           = "�󌱔ԍ�";
        $header["APPLICANTDIV"]     = "�������x";
        $header["TESTDIV"]          = "�����敪";
        $header["TESTDIV1"]         = "�󌱋敪";
        $header["NAME"]             = "����";
        $header["NAME_KANA"]        = "�ӂ肪��";
        $header["BIRTHDAY"]         = "���N����";
        $header["FINSCHOOL_NAME"]   = "�o�g���w�Z";
        $header["SEQ034_R1"]        = "���i���e";
        $header["SEQ034_R2"]        = "���i";
        $header["SEQ034_R3"]        = "���Z�L��";
        $header["SEQ034_R4"]        = "����";

        //�N���A
        $contents  = "";
        //�w�b�_�o��
        $contents .= implode($header, ",")."\n";
        $seq034R2 = array('1' => '����', '2' => '���E');
        $seq034R3 = array('1' => '���ݒ�', '2' => '�L��', '3' => '����');
        $seq034R4 = array('1' => '���ݒ�', '2' => '��', '3' => '��');

        $query = knjl302eQuery::selectCsvQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row["SEQ034_R2"] = $seq034R2[$row["SEQ034_R2"]];
            $row["SEQ034_R3"] = $seq034R3[$row["SEQ034_R3"]];
            $row["SEQ034_R4"] = $seq034R4[$row["SEQ034_R4"]];
            $row["BIRTHDAY"] = str_replace('-', '/', $row["BIRTHDAY"]);
            $row = str_replace("\r\n","",str_replace(",",".",$row));   //�f�[�^���ɃJ���}������΃R�����ɒu���E�E�E�u�����镶���́A�J���}�łȂ���Ή��ł��ǂ��B
            $array = $header;

            //�w�b�_���ɒl���Z�b�g
            $sep = "";
            foreach ($array as $field => $label) {
                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            $contents .= "\n";

            $header_data = true;
        }
        $result->free();

        /**************************/
        /*  �t�@�C�����p���̎擾  */
        /**************************/
        //�������x
        $applicantdivName = $db->getOne(knjl302eQuery::getNameMstNamecd2($this, "L003", $this->field["APPLICANTDIV"]));
        //�����敪
        $TestdivName = $db->getOne(knjl302eQuery::getNameMstNamecd2($this, "L004", $this->field["TESTDIV"]));
        //�t�@�C����
        $title = $this->ObjYear.'�N�x '.$applicantdivName.$TestdivName.'�f�[�^';

        Query::dbCheckIn($db);

        //�b�r�u�o��
        if ($header_data) {
            $filename = sprintf("%s.csv", $title);
            $contents = i18n_convert($contents,"SJIS-win");

            if (stristr($_SERVER['HTTP_USER_AGENT'], "MSIE") || stristr($_SERVER['HTTP_USER_AGENT'], "Trident")) {
                // IE�̏ꍇ
                $filename = mb_convert_encoding($filename,"SJIS-win");
                header("Content-Disposition: inline; filename=$filename");
            } else {
                header("Content-Disposition: attachment; filename*=UTF-8''".rawurlencode($filename));
            }
            i18n_http_output("pass");
            header("Accept-Ranges: none");
            header("Content-Transfer-Encoding: binary");
            header("Content-Length: ". strlen($contents) );
            header("Content-Type: text/octet-stream");

            echo $contents;
            return true;
        } else {
            $this->warning = "�Y���f�[�^�́A����܂���B";
            return false;
        }
    }
}
?>

<?php

require_once('for_php7.php');

class knjl081rModel extends Model {
    var $field  = array();
    var $data   = array();
    var $cmd;
    var $file;
    var $ObjYear;

    function init() {
        $this->ObjYear   = CTRL_YEAR + 1;
        $this->exam_type = '1'; //固定

        $this->fixApplicantDiv = '1';
        if (SCHOOLKIND == 'J') {
            $this->fixApplicantDiv = '2';
        }

        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),
                             "TESTDIV"      =>  VARS::post("TESTDIV")
                             );

        $this->cmd = VARS::request("cmd");

    }

    /********************/
    /*  ＣＳＶ出力処理  */
    /********************/
    function getDownloadModel() {
        $db = Query::dbCheckOut();
        $col = array();

        $col["RECEPTNO"]        = "受験番号";
        $col["GOUHI_HANTEI_CD"] = "合否判定コード";

        //クリア
        $contents  = "";

        $query = knjl081rQuery::selectCsvQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace("\r\n","",str_replace(",",".",$row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

            $array = $col;

            //値をセット
            $sep = "";
            foreach ($array as $field => $label) {
                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            $contents .= "\n";

            $data = true;
        }
        $result->free();

        /**************************/
        /*  ファイル名用名称取得  */
        /**************************/
        //入試制度
        $applicantdivName = $db->getOne(knjl081rQuery::getNameMstNamecd2($this, "L003", $this->field["APPLICANTDIV"]));
        //入試区分
        $TestdivName = $db->getOne(knjl081rQuery::getNameMstNamecd2($this, $this->setNameCd1, $this->field["TESTDIV"]));
        //ファイル名
        $title = $this->ObjYear.'年度 '.$applicantdivName.$TestdivName.'合否判定データ';

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($data) {
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
?>

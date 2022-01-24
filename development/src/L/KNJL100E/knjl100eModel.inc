<?php

require_once('for_php7.php');

class knjl100eModel extends Model
{
    public $field  = array();
    public $data   = array();
    public $cmd;
    public $file;
    public $ObjYear;

    public function init()
    {
        $this->ObjYear = CTRL_YEAR + 1;

        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),
                             "TESTDIV"      =>  VARS::post("TESTDIV"),
                             "CHECK1"       =>  VARS::post("CHECK1")
                             );

        $this->cmd = VARS::request("cmd");
    }

    /********************/
    /*  ＣＳＶ出力処理  */
    /********************/
    public function getDownloadModel()
    {
        $db = Query::dbCheckOut();
        $col = array();

        $col["EXAMNO"]           = "受験番号";
        $col["RENKEI_FORMAT"]    = "出願コース＋受験区分＋合格コース＋合格区分";

        //クリア
        $contents  = "";

        $query = knjl100eQuery::selectCsvQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $setPass = "9998";  //欠席
            if ($row["JUDGEMENT"] == "1") {
                $setPass = sprintf("%02d", $row["SEQ030_R5"]).sprintf("%02d", $row["SEQ030_R4"]);  //合格
            } elseif ($row["JUDGEMENT"] == "2") {
                $setPass = "9999";  //不合格
            } elseif ($row["JUDGEMENT"] == "4") {
                $setPass = "9997";  //追試
            }
            $row["RENKEI_FORMAT"] = sprintf("%02d", $row["DESIREDIV"]).sprintf("%02d", $row["TESTDIV1"]).$setPass;
            $row = str_replace("\r\n", "", str_replace(",", ".", $row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $array = $col;

            if ($this->field["CHECK1"] == "on") {
                $row["RENKEI_FORMAT"] = "9".$row["RENKEI_FORMAT"];
            }

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
        $applicantdivName = $db->getOne(knjl100eQuery::getNameMstNamecd2($this, "L003", $this->field["APPLICANTDIV"]));
        //入試区分
        if ($model->field["TESTDIV"] == '1') {
            $TestdivName = "推薦";
        } elseif ($model->field["TESTDIV"] == '2') {
            $TestdivName = "一般";
        } elseif ($model->field["TESTDIV"] == '3') {
            $TestdivName = "二次";
        }
        //ファイル名
        $title = $this->ObjYear.'年度 '.$applicantdivName.$TestdivName.'合否結果データ';

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

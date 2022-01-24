<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl306gModel extends Model {
    var $field = array();
    var $cmd;
    var $control;
    var $isKeiai;
    var $isKasiwara;

    /**
     * メンバ変数の初期化
     */
    function knjl306gModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("APPLICANTDIV"      =>  VARS::post("APPLICANTDIV"),
                             "TESTDIV"           =>  VARS::post("TESTDIV")
                            );
        $this->ObjYear = CTRL_YEAR + 1;

        $this->cmd = VARS::request("cmd");

        //学校コードを取得　敬愛：30270254001、柏原：30270247001
        $rtnRow = knjl306gQuery::getNameMstZ010();
        $this->isKeiai      = $rtnRow["NAME2"] == "30270254001" ? "1" : "";
        $this->isKasiwara   = $rtnRow["NAME2"] == "30270247001" ? "1" : "";
    }

    //ＣＳＶ出力
    function getDownloadModel() {
        $db = Query::dbCheckOut();

        $applicantName = $db->getOne(knjl306gQuery::getNameMst($this->ObjYear, "L003", $this->field["APPLICANTDIV"]));
        $testDivName = $db->getOne(knjl306gQuery::getNameMst($this->ObjYear, "L004", $this->field["TESTDIV"]));

        //ヘッダ
        $setNendoDate = $this->ObjYear."-04-01";
        $setNendo = common::DateConv1(str_replace("-", "/", $setNendoDate), 11);
        $setTitle = "{$setNendo}　{$applicantName}　{$testDivName}　受験者データ";
        $contents = ",{$setTitle}\n";
        $contents .= "会場名,受験番号,氏名\n";

        $data = false;
        $result = $db->query(knjl306gQuery::getOutputQueryCsv($this));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $contents .= implode($row, ",") ."\n";
            $data = true;
        }
        Query::dbCheckIn($db);

        if ($data) {
            $filename = sprintf("%s.csv", "{$setTitle}");
            common::downloadContents($contents, $filename);

            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
?>

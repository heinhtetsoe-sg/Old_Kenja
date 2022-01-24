<?php

require_once('for_php7.php');

    //定数定義
//配列内の数値のみ取得
function num_filter($var) {
    return (is_numeric($var));
}

class knjl111nModel extends Model {
    var $cmd;
    var $control_data = array();        //コントロールのデータを保持
    var $field = array();
    var $entexamYear;

    function knjl111nModel() {
        //学籍処理日取得
        common::GetControlMaster_Fnc($this->control_data);
    }

    function init() {
        $this->cmd = VARS::request("cmd");
        $this->entexamYear = CTRL_YEAR + 1;

        $this->field = array("APPLICANTDIV" => VARS::post("APPLICANTDIV"),
                             "TESTDIV"      => VARS::post("TESTDIV")
                            );

        $this->getPropertiesAll(); //プロパティファイルの読込み
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        knjl111nQuery::getDeleteQuery($db, $this);

        $this->setFields = array();
        foreach ($this->shDiv as $shDiv => $shName) {
            foreach ($shName["COURSE"] as $courseCd => $courseName) {
                foreach ($this->subclassCd as $subclassCd => $subclassName) {
                    $soeji = $shDiv."_".$courseCd."_".$subclassCd;
                    $this->setFields[$soeji] = VARS::post("SCORE".$soeji);
                }
            }
        }

        knjl111nQuery::getUpdateQuery($db, $this);

        $db->commit();
        Query::dbCheckIn($db);

        $this->setMessage("MSG201");
    }

}

?>

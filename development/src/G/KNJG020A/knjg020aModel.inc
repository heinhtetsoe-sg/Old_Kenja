<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjg020aModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjg020aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd      = STAFFCD;                  //職員コード
    }
    function init()
    {               
        $this->field = array("STD_DIV"        =>  VARS::post("STD_DIV"),        //ラジオ選択データ（卒業生/在学生）
                             "PAGE_CNT"       =>  VARS::post("PAGE_CNT"),       //ページ総数
                             "DBNAME"         =>  VARS::post("DBNAME"),         //DB変更に備えて
                     );

        $this->cmd         = VARS::request("cmd");

        for ($pi = 1; $pi <= $this->field["PAGE_CNT"]; $pi++) {
            $this->field["UPD_CHECK"][$pi] = VARS::post("UPD_CHECK_{$pi}");
        }

        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if (VARS::post("SCHOOL_KIND")) {
            $this->setSchoolKind = VARS::post("SCHOOL_KIND");
        }
    }

    function getUpdateModel() {

        //DB接続
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if (is_array($this->field["UPD_CHECK"])) {
            foreach ($this->field["UPD_CHECK"] as $page => $val) {
                if ($val) {
                    foreach ($this->printData[$page] as $line => $certifData) {
                        $query = knjg020aQuery::getDetailDat($this, $certifData);
                        $detailCnt = $db->getOne($query);

                        $query = knjg020aQuery::updatePrintZumi($this, $certifData, $detailCnt);
                        $db->query($query);
                    }
                }
            }
        }

        $db->commit();
        //DB切断
        Query::dbCheckIn($db);
    }
}
?>

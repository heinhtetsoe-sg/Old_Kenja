<?php

require_once('for_php7.php');


class knjd214sModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;
    var $staffcd;
    var $selectdata;
    var $del_check = array();
    var $school_kind;

    function knjd214sModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd      = STAFFCD;                  //職員コード
    }

    function init() {
        $this->field = array("COPY_SAKI_CD"     =>  "9990009",                      //コピー先(学年評定)
                             "KARI_DIV"         =>  VARS::post("KARI_DIV"),         //仮評定フラグ
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "SOUGAKU"          =>  VARS::post("SOUGAKU"),          //総学等も含む
                             "CONVERT"          =>  VARS::post("CONVERT"),          //評定換算する
                             "TESTKINDCD"       =>  VARS::post("TESTKINDCD"),       //テスト
                             "SHORI"            =>  VARS::post("SHORI")             //クリア処理とコピー処理のラジオ
                     );

        $this->selectdata = VARS::post("selectdata");

        $this->cmd         = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();

        //実行履歴データ削除
        $this->del_check = array();
        $this->del_check = VARS::post("DEL_CHECK");
    }

    function validateInput() {
        $db = Query::dbCheckOut();
        //NULLではないデータが１件でもあれば、エラーメッセージを表示。
        $query  = knjd214sQuery::getClearExists($this, CTRL_YEAR);
        $dataCnt = $db->getOne($query);
        Query::dbCheckIn($db);
        if (0 < $dataCnt) {
            $this->setWarning("MSG306", "クリア処理が行われていません。");
            return false;
        }

        return true;
    }

    function &getCopyModel() {
        if ($this->validateInput()){
            //①学年評定
            knjd214sQuery::getCopyQuery($this);
            knjd214sQuery::getCombinedCopyQuery($this);
            //②仮評定
            //仮評定のレコードを固定で、'x-xxxx-09'を作成（コピー元の小分類を'09'として）
            $code = $this->field["TESTKINDCD"];
            $sem  = substr($code, 0, 1);
            $kind = substr($code, 1, 2);
            $item = substr($code, 3, 2);
            $div  = "09";
            $this->field["KARI_TESTCD"] = $sem . $kind . $item . $div;
            //但し、9学期は、対象外（念のため、コピー元と同じ場合も対象外）
            if ($sem != "9" && $this->field["KARI_TESTCD"] != $this->field["TESTKINDCD"]) {
                knjd214sQuery::getProvCopyQuery($this);
            } else {
                $this->field["KARI_TESTCD"] = "";
            }
            //③実行履歴
            knjd214sQuery::executeRireki($this);

            $this->setMessage("MSG201");
        }
    }

    function &getClearModel() {
        knjd214sQuery::executeRireki($this);
        knjd214sQuery::getClearQuery($this);
        $this->setMessage("MSG201");
    }

    function &getDeleteRirekiModel() {
        if (get_count($this->del_check)) {
            knjd214sQuery::deleteRireki($this);
            $this->setMessage("MSG201");
        }
    }
}
?>

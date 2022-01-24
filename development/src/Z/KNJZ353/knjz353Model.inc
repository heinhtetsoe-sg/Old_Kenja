<?php

require_once('for_php7.php');

class knjz353Model extends Model {
    var $field = array();
    var $cmd;
    var $selectdata1;

    //メンバ変数の初期化
    function knjz353Model() {}

    function init() {
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            unset($this->school_kind);
        }

        $this->field["PRG_ID"]          = VARS::post("PRG_ID");
        if (trim($this->field["PRG_ID"]) == "" ) $this->field["PRG_ID"] = "KNJF030";
        $this->field["GROUPCD"]         = VARS::post("GROUPCD");
        $this->field["GRADE"]           = "00";
        $this->selectdata1              = VARS::post("selectdata1");
        $this->itemList                 = VARS::post("itemList");
        $this->cmd                      = VARS::request("cmd");

        //プログラムID => ATTEND_DIV
        $this->prgid = array();
        $this->prgid["KNJF030"]  = "1";
        $this->prgid["KNJF030H"] = "2";

        //出欠項目一覧
        $this->medexamItem = array();
        if ($this->field["PRG_ID"] == "KNJF030") {
            $this->medexamItem[]["1"]                      = array("0000", "",     array("", "健康診断票（一般)"), array("", ""));
            $this->medexamItem[]["2"]                      = array("0000", "",     array("", "健康診断票（歯・口腔）"), array("", ""));
            $this->medexamItem[]["3"]                      = array("0000", "",     array("1", "健康診断の未受検項目のある生徒へ"), array("", ""));
            $this->medexamItem[]["4"]                      = array("0000", "",     array("1", "眼科検診のお知らせ"), array("", ""));
            $this->medexamItem[]["5"]                      = array("0000", "",     array("1", "検診結果のお知らせ（歯・口腔）"), array("", ""));
            $this->medexamItem[]["6"]                      = array("0000", "",     array("1", "定期健康診断結果"), array("", ""));
            $this->medexamItem[]["7"]                      = array("0000", "",     array("1", "検診結果のお知らせ（一般）"), array("", ""));
            $this->medexamItem[]["8"]                      = array("0000", "",     array("1", "内科検診所見あり生徒の名簿"), array("", ""));
            $this->medexamItem[]["9"]                      = array("0000", "",     array("1", "定期健康診断異常者一覧表"), array("", ""));
        } else if ($this->field["PRG_ID"] == "KNJF030H" ){
            $this->medexamItem[]["1"]                      = array("0000", "",       array("", ""), array("", "健康診断票（一般）"));
            $this->medexamItem[]["2"]                      = array("0000", "",       array("", ""), array("", "健康診断票（歯・口腔)"));
            $this->medexamItem[]["3"]                      = array("0000", "",       array("", ""), array("1", "眼科検診結果のお知らせ"));
            $this->medexamItem[]["4"]                      = array("0000", "",       array("", ""), array("1", "力検査結果のお知らせ"));
            $this->medexamItem[]["5"]                      = array("0000", "",       array("", ""), array("1", "聴力検査結果のお知らせ"));
            $this->medexamItem[]["6"]                      = array("0000", "",       array("", ""), array("1", "耳鼻咽頭検診結果のお知らせ"));
            $this->medexamItem[]["7"]                      = array("0000", "",       array("", ""), array("1", "歯科検診検査結果のお知らせ"));
            $this->medexamItem[]["8"]                      = array("0000", "",       array("", ""), array("1", "歯科検診検査結果のお知らせ（全員配布）"));
            $this->medexamItem[]["9"]                      = array("0000", "",       array("", ""), array("1", "期健康診断結果のお知らせ"));
            $this->medexamItem[]["10"]                     = array("0000", "",       array("", ""), array("1", "内科検診結果のお知らせ"));
            $this->medexamItem[]["11"]                     = array("0000", "",       array("", ""), array("1", "運動器検診結果のお知らせ"));
            $this->medexamItem[]["12"]                     = array("0000", "",       array("", ""), array("1", "色覚検査結果のお知らせ"));
            $this->medexamItem[]["13"]                     = array("0000", "",       array("", ""), array("1", "心臓検診結果のお知らせ"));
            $this->medexamItem[]["14"]                     = array("0000", "",       array("", ""), array("1", "心臓検診精密検査のお願い（主治医）"));
            $this->medexamItem[]["15"]                     = array("0000", "",       array("", ""), array("1", "胸部エックス線検診結果のお知らせ（要経過観察）"));
            $this->medexamItem[]["16"]                     = array("0000", "",       array("", ""), array("1", "胸部エックス線検診結果のお知らせ（要精密検査）"));
            $this->medexamItem[]["17"]                     = array("0000", "",       array("", ""), array("1", "胸部エックス線検診結果のお知らせ（要精密検査 主治医）"));
            $this->medexamItem[]["18"]                     = array("0000", "",       array("", ""), array("1", "未検診のお知らせ（歯科）"));
            $this->medexamItem[]["19"]                     = array("0000", "",       array("", ""), array("1", "未検診のお知らせ（内科 眼科 耳鼻科）"));
            $this->medexamItem[]["20"]                     = array("0000", "",       array("", ""), array("1", "尿検査結果のお知らせ"));
        }

        //学校種別
        if (VARS::post("SCHOOL_KIND")) {
            $this->school_kind = VARS::post("SCHOOL_KIND");
        }
    }

    //入力項目チェック
    function validateInput() {
        if (!strlen($this->field["PRG_ID"])) {
            $this->warning = "対象プログラムを選択してください。";
            return false;
        }

        
        if ($this->cmd == "copy" && knjz353Query::getExists(CTRL_YEAR)) {
            $this->setWarning("MSG203","今年度のデータが存在します。");
            return false;
        }

        if ($this->cmd == "copy" && !knjz353Query::getExists(CTRL_YEAR - 1)) {
            $this->setWarning("MSG203","前年度のデータが存在しません。");
            return false;
        }

        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
            $this->setMessage("MSG201");
        }
    }

    //アップデート時の内部メソッド
    function &update() {
        if ($this->cmd == "update") {
            $selectdata = $this->selectdata1;
        }
        //更新処理
        knjz353Query::getUpdateQuery($this, $selectdata, $flg);
    }

    //前年度からのコピーのモデルを返します
    function &getCopyModel() {
        if ($this->validateInput()) {
            knjz353Query::getCopyQuery($this);
            $this->setMessage("MSG201");
        }
    }
}
?>

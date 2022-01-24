<?php

require_once('for_php7.php');


class knjd128lModel extends Model {
    var $field = array();
    var $data  = array();
    var $testcdArray  = array();
    var $cmd;
    var $usePerfect;
    var $chikokuHyoujiFlg;
    var $useSlumpHyouka;
    var $subclassAll;
    var $fieldsCalc = array();
    var $testcdMotoArray = array();

    function knjd128lModel() {
    }

    function init() {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            $this->data = array();
            $this->testcdArray = array();
            $this->testcdMotoArray = array();
            common::GetControlMaster_Fnc($this->control);
        }
        $this->field["MOVE_ENTER"]  = VARS::post("MOVE_ENTER"); //エンター押下時の移動方向
        $this->cmd     = VARS::request("cmd");
        //科目コード
        $this->field["SUBCLASSCD"] = VARS::request("SUBCLASSCD");
        if ($this->cmd == "update") {
            $this->field["SUBCLASSCD"] = VARS::post("H_SUBCLASSCD");
        }
        //講座コード
        if ($this->cmd!="subclasscd"&&VARS::request("CHAIRCD")) {
            $this->field["CHAIRCD"] = VARS::request("CHAIRCD");
        } else if ($this->cmd == "update") {
            $this->field["CHAIRCD"] = VARS::post("H_CHAIRCD");
        } else {
            $this->field["CHAIRCD"] = "";
        }
        $this->field["SIDOU_CHK"] = VARS::post("SIDOU_CHK");
        $this->field["KEEKA_OVER"] = VARS::post("KEEKA_OVER");
        $this->field["COUNTER"]  = VARS::post("COUNTER"); //行数

        //プロパティファイル
        $this->getPropertiesAll();
        //初期値の設定
        $this->usePerfect = (strlen($this->Properties["usePerfect"])) ? $this->Properties["usePerfect"] : false;
        $this->chikokuHyoujiFlg = (strlen($this->Properties["chikokuHyoujiFlg"])) ? $this->Properties["chikokuHyoujiFlg"] : 0;
        //「評価にリンクを付けるか？」のフラグを取得
        $rtnRow = knjd128lQuery::getNameMstD048();
        $this->useSlumpHyouka = $rtnRow["NAMESPARE1"] == "1" ? true : false; // 1:評価にリンクを付ける

        //算出ボタン押し下げ時のみ保持（それ以外は初期化）
        $this->fieldsCalc = array();
    }

    //入力項目チェック
    function validateInput() {
        return true;
    }

    function &getUpdateModel() {
        //初期化
        $this->fields = array();
        $this->testInsArray = array();
        for ($i = 0; $i < $this->field["COUNTER"]; $i++) {
            //学籍番号を更新時に取得
            $this->fields["SCHREGNO"][] = VARS::post("SCHREGNO"."-".$i);
            foreach ($this->testcdArray as $key => $codeArray) {
                $testcd = $codeArray["TESTCD"];

                $score = "SCORE".$testcd."-".$i;
                $this->fields["SCORE".$testcd][] = VARS::post($score);
                $this->field["CHK_COMP".$testcd] = VARS::post("CHK_COMP".$testcd);

                //履修単位・修得単位
                if ($testcd == "9990009") {
                    $this->fields["COMP_CREDIT"][] = VARS::post("COMP_CREDIT"."-".$i);
                    $this->fields["GET_CREDIT"][] = VARS::post("GET_CREDIT"."-".$i);
                    //仮評定フラグ
                    $this->fields["PROV_FLG"][] = VARS::post("PROV_FLG"."-".$i);
                }

                //成績もしくは単位数が１つでも入力された考査コードを配列に保管
                if (strlen(VARS::post($score)) || $testcd == "9990009" && (strlen(VARS::post("COMP_CREDIT"."-".$i)) || strlen(VARS::post("GET_CREDIT"."-".$i)))) $this->testInsArray[$testcd] = $testcd;
            }
        }

        if ($this->validateInput()) {
            knjd128lQuery::update($this);
            knjd128lQuery::updateRecordChkfin($this);
            $this->setMessage("MSG201");
        }
    }

    function &getCalcModel($calcMethod) {
        //初期化
        $this->fieldsCalc = array();
        //画面上から取得
        for ($i = 0; $i < $this->field["COUNTER"]; $i++) {
            foreach ($this->testcdArray as $key => $codeArray) {
                $name = "SCORE".$codeArray["TESTCD"]."-".$i;
                $this->fieldsCalc[$name] = VARS::post($name);
            }
        }
        //学期評価・学年評価を算出
        for ($i = 0; $i < $this->field["COUNTER"]; $i++) {
            foreach ($this->testcdMotoArray as $testcd => $array) {
                $abcentFlg = false;
                $term_data = array();
                foreach ($array as $key => $codeArray) {
                    $nameMoto = "SCORE".$codeArray["MOTO_TESTCD"]."-".$i;
                    $score = $this->fieldsCalc[$nameMoto];
                    $nameAss = "SCORE".$codeArray["MOTO_TESTCD"]."_ASSESSLOW2"."-".$i;
                    $assesslow2 = (strlen($this->field["SIDOU_CHK"])) ? VARS::post($nameAss) : "";
                    //数値セット
                    if (is_numeric($assesslow2) && $score < $assesslow2) {
                       $term_data[] = $assesslow2;
                    } else if (is_numeric($score)) {
                       $term_data[] = $score;
                    }
                    //欠席フラグ
                    if ($score == "*" && !is_numeric($assesslow2)) {
                       $abcentFlg = true;
                    }
                }
                //平均
                $name = "SCORE".$testcd."-".$i;
                if (get_count($term_data)) {
                    $avg = array_sum($term_data)/get_count($term_data);
                    //評価の算出方法
                    if ($calcMethod == "3") {
                        $this->fieldsCalc[$name] = floor($avg); //3:切り捨て
                    } else if ($calcMethod == "2") {
                        $this->fieldsCalc[$name] = ceil($avg);  //2:切り上げ
                    } else {
                        $this->fieldsCalc[$name] = round($avg); //1:四捨五入(初期値)
                    }
                    //欠席フラグの場合、空白とする
                    if ($abcentFlg) {
                        $this->fieldsCalc[$name] = ""; //空白
                    }
                } else {
                    $this->fieldsCalc[$name] = ""; //空白
                }
            }
        }

        if ($this->validateInput()) {
            $this->setMessage("","算出しました。\\n更新ボタンを押下しないとデータは保存されません。");
        }
    }
}
//var_dump($_REQUEST);
?>

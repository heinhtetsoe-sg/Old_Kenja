<?php

require_once('for_php7.php');

//定数定義
class knjz095kModel extends Model {
    var $field = array();
    var $cmd;
    var $prefecturescd; //都道府県コード
    var $grade;         //学年
    var $year;

    //メンバ変数の初期化
    function knjz095kModel() {}

    function init() {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->prefecturescd);
            unset($this->grade);
        }

        if (VARS::post("YEAR")) {
            $this->year = VARS::post("YEAR");
        }
        $this->cmd = VARS::request("cmd");

        if (VARS::get("PREFECTURESCD")) {
            $this->prefecturescd = VARS::get("PREFECTURESCD");
        }
        if (VARS::get("GRADE")) {
            $this->grade = VARS::get("GRADE");
        }

        $this->field = array("YEAR"             => $this->year,
                             "PREFECTURESCD"    => VARS::post("PREFECTURESCD"),     //都道府県コード
                             "GRADE"            => VARS::post("GRADE"),             //学年
                             "CURRICULUM_FLG"   => VARS::post("CURRICULUM_FLG"),    //入学年度使用
                             "THIS_YEAR_FLG"    => VARS::post("THIS_YEAR_FLG"),     //今年度使用
                             "USE_RANK"         => VARS::post("USE_RANK"),          //ランク使用
                             "ZENKI_KAISI_YEAR" => VARS::post("ZENKI_KAISI_YEAR"),  //前期開始年度
                             "KOUKI_KAISI_YEAR" => VARS::post("KOUKI_KAISI_YEAR")   //後期開始年度
                             );
    }

    //入力項目チェック
    function validateInput($kind) {
        if ($kind == "insert" || $kind == "update") {
            //空文字チェック
            if (strlen($this->field["YEAR"]) < 4) {
                $this->setWarning("MSG301","( 年度 )");
                return false;
            }
            if (strlen($this->field["PREFECTURESCD"]) == 0) {
                $this->setWarning("MSG301","( 都道府県コード )");
                return false;
            }
            if (strlen($this->field["GRADE"]) == 0) {
                $this->setWarning("MSG301","( 学年 )");
                return false;
            }
            //ランク使用チェック時のチェック
            if ($this->field["USE_RANK"] != "") {
                if (strlen($this->field["ZENKI_KAISI_YEAR"]) == 0) {
                    $this->setWarning("MSG301","( 前期開始年度 )");
                    return false;
                }
                if (strlen($this->field["KOUKI_KAISI_YEAR"]) == 0) {
                    $this->setWarning("MSG301","( 後期開始年度 )");
                    return false;
                }
            }
            //文字数チェック
            if (strlen($this->field["ZENKI_KAISI_YEAR"]) != 0 && strlen($this->field["ZENKI_KAISI_YEAR"]) != 4) {
                $this->setWarning("MSG203","( 前期開始年度は西暦4桁で指定して下さい。)");
                return false;
            }
            if (strlen($this->field["KOUKI_KAISI_YEAR"]) != 0 && strlen($this->field["KOUKI_KAISI_YEAR"]) != 4) {
                $this->setWarning("MSG203","( 後期開始年度は西暦4桁で指定して下さい。)");
                return false;
            }
        }

        if (($kind=="update" || $kind=="delete") && ($this->prefecturescd != $this->field["PREFECTURESCD"] ||
                                                    $this->grade != $this->field["GRADE"])) {
            $this->setWarning("MSG308");
            return false;
        }
        
        //存在チェック
        $row = knjz095kQuery::getRow($this,2);      //入力項目の１レコード取得
        if (($this->cmd == "update" || $this->cmd == "delete") && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        return true;
    }

    //追加処理
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput("insert")) {
            $reduction_seq = knjz095kQuery::getInsertQuery($this->field);
            $this->prefecturescd    = $this->field["PREFECTURESCD"];
            $this->grade            = $this->field["GRADE"];
            
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput("update")) {
            knjz095kQuery::getUpdateQuery($this->field,$this->reduction_seq);
            $this->prefecturescd    = $this->field["PREFECTURESCD"];
            $this->grade            = $this->field["GRADE"];

            $this->setMessage("MSG201");
        }
        return;
    }

    //削除処理
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput("delete")) {
            knjz095kQuery::getDeleteQuery($this->field,$this->reduction_seq);
            $this->prefecturescd    = "";
            $this->grade            = "";

            $this->setMessage("MSG201");
        }
        return;
    }

    //前年度のデータをコピー
    function &getCopyModel() {
        //今年度と今年度のデータ存在チェック
        $check = array();
        $check = knjz095kQuery::CheckData($this->year);

        if (in_array('X',$check)) {
            $this->setWarning('MSG203', "今年度のデータが存在します。");
            return false;
        } elseif (!in_array('Y',$check)) {
            $this->setWarning('MSG203', "前年データが存在しません。");
            return false;
        } else{
            knjz095kQuery::getCopyQuery($this->year);
            $this->setMessage("MSG201");
        }
        return;
    }

    //変数をクリアします
    function clean() {}
}
?>

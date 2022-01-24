<?php

require_once('for_php7.php');

//定数定義
class knjz092kModel extends Model {
    var $field = array();
    var $cmd;
    var $prefecturescd; //都道府県コード
    var $grade;         //学年
    var $reductionmoney_1;//軽減額1
    var $reductionmoney_2;//軽減額2
    var $year;

    //メンバ変数の初期化
    function knjz092kModel() {}

    function init() {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->prefecturescd);
            unset($this->grade);
            unset($this->rankDiv);
            unset($this->reductionmoney_1);
            unset($this->minMoney_1);
            unset($this->income_low);
            unset($this->income_high);
            unset($this->reductionmoney_2);
            unset($this->minMoney_2);
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
        if (VARS::get("RANK_DIV")) {
            $this->rankDiv = VARS::get("RANK_DIV");
        }
        if (VARS::get("REDUCTION_SEQ")) {
            $this->reduction_seq = VARS::get("REDUCTION_SEQ");
        }
        if (VARS::get("REDUCTIONMONEY_1")) {
            $this->reductionmoney_1 = VARS::get("REDUCTIONMONEY_1");
        }
        if (VARS::get("MINMONEY_1")) {
            $this->minMoney_1 = VARS::get("MINMONEY_1");
        }
        if (VARS::get("INCOME_LOW")) {
            $this->income_low = VARS::get("INCOME_LOW");
        } elseif (!VARS::get("INCOME_LOW")) {
            $this->income_low = 0;
        }
        if (VARS::get("INCOME_HIGH")) {
            $this->income_high = VARS::get("INCOME_HIGH");
        }
        if (VARS::get("REDUCTIONMONEY_2")) {
            $this->reductionmoney_2 = VARS::get("REDUCTIONMONEY_2");
        }
        if (VARS::get("MINMONEY_2")) {
            $this->minMoney_2 = VARS::get("MINMONEY_2");
        }

        $this->field = array("YEAR"             => $this->year,
                             "PREFECTURESCD"    => VARS::post("PREFECTURESCD"),     //都道府県コード
                             "GRADE"            => VARS::post("GRADE"),             //学年
                             "RANK_DIV"         => VARS::post("RANK_DIV"),          //ランク
                             "REDUCTIONMONEY_1" => VARS::post("REDUCTIONMONEY_1"),  //軽減額1
                             "REDUCTIONMONEY_2" => VARS::post("REDUCTIONMONEY_2"),  //軽減額2
                             "MIN_MONEY_1"      => VARS::post("MIN_MONEY_1"),       //下限額1
                             "MIN_MONEY_2"      => VARS::post("MIN_MONEY_2"),       //下限額2
                             "PARENTS_MONEY_1"  => VARS::post("PARENTS_MONEY_1"),   //保護者負担金1
                             "PARENTS_MONEY_2"  => VARS::post("PARENTS_MONEY_2")    //保護者負担金2
                             );

        $this->field["MAX_MONEY"] = $this->field["REDUCTIONMONEY_1"] + $this->field["REDUCTIONMONEY_2"];
        $this->field["MIN_MONEY"] = $this->field["MIN_MONEY_1"] + $this->field["MIN_MONEY_2"];
    }

    //入力項目チェック
    function validateInput($db, $kind) {
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
            if (strlen($this->field["RANK_DIV"]) == 0) {
                $this->setWarning("MSG301","( ランク )");
                return false;
            }
            if (strlen($this->field["REDUCTIONMONEY_1"]) == 0) {
                $this->setWarning("MSG301","( 上限額1 )");
                return false;
            }
            if (strlen($this->field["REDUCTIONMONEY_2"]) == 0) {
                $this->setWarning("MSG301","( 上限額2 )");
                return false;
            }
            //文字数チェック
            if (strlen($this->field["REDUCTIONMONEY_1"]) > 8) {
                $this->setWarning("MSG915","( 上限額1は8バイトまでです。)");
                return false;
            }
            if (strlen($this->field["REDUCTIONMONEY_2"]) > 8) {
                $this->setWarning("MSG915","( 上限額2は8バイトまでです。)");
                return false;
            }

            if (strlen($this->field["MIN_MONEY_1"]) == 0) {
                $this->setWarning("MSG301","( 下限額1 )");
                return false;
            }
            if (strlen($this->field["MIN_MONEY_2"]) == 0) {
                $this->setWarning("MSG301","( 下限額2 )");
                return false;
            }
            //文字数チェック
            if (strlen($this->field["MIN_MONEY_1"]) > 8) {
                $this->setWarning("MSG915","( 下限額1は8バイトまでです。)");
                return false;
            }
            if (strlen($this->field["MIN_MONEY_2"]) > 8) {
                $this->setWarning("MSG915","( 下限額2は8バイトまでです。)");
                return false;
            }
        }

        if (($kind=="update" || $kind=="delete") &&
            ($this->prefecturescd != $this->field["PREFECTURESCD"] ||
             $this->grade != $this->field["GRADE"] ||
             $this->rankDiv != $this->field["RANK_DIV"])
        ) {
            $this->setWarning("MSG308");
            return false;
        }

        $query = knjz092kQuery::getRow($this, 2);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        if (($kind == "insert") && is_array($row)) {
            $this->setWarning("MSG302");
            return false;
        }

        //存在チェック
        if (($this->cmd == "update" || $this->cmd == "delete") && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }

        return true;
    }

    //追加処理
    function &getInsertModel() {

        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        //入力チェック
        if ($this->validateInput($db, "insert")) {
            $query = knjz092kQuery::getInsertQuery($this->field);
            $db->query($query);
            $this->prefecturescd    = $this->field["PREFECTURESCD"];
            $this->grade            = $this->field["GRADE"];
            $this->rankDiv          = $this->field["RANK_DIV"];
            $this->reductionmoney_1 = $this->field["REDUCTIONMONEY_1"];
            $this->reductionmoney_2 = $this->field["REDUCTIONMONEY_2"];
            $this->minMoney_1       = $this->field["MINMONEY_1"];
            $this->minMoney_2       = $this->field["MINMONEY_2"];
            $this->reduction_seq    = $reduction_seq;
            $this->setMessage("MSG201");
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //更新処理
    function &getUpdateModel() {

        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        //入力チェック
        if ($this->validateInput($db, "update")) {
            $query = knjz092kQuery::getUpdateQuery($this->field);
            $db->query($query);
            $this->prefecturescd    = $this->field["PREFECTURESCD"];
            $this->grade            = $this->field["GRADE"];
            $this->rankDiv          = $this->field["RANK_DIV"];
            $this->reductionmoney_1 = $this->field["REDUCTIONMONEY_1"];
            $this->reductionmoney_2 = $this->field["REDUCTIONMONEY_2"];
            $this->minMoney_1       = $this->field["MINMONEY_1"];
            $this->minMoney_2       = $this->field["MINMONEY_2"];
            $this->setMessage("MSG201");
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //削除処理
    function &getDeleteModel() {

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //入力チェック
        if ($this->validateInput($db, "delete")) {
            $query = knjz092kQuery::getDeleteQuery($this->field);
            $db->query($query);
            $this->prefecturescd    = "";
            $this->grade            = "";
            $this->rankDiv          = "";
            $this->reductionmoney_1 = "";
            $this->reductionmoney_2 = "";
            $this->minMoney_1       = "";
            $this->minMoney_2       = "";
            $this->setMessage("MSG201");
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //前年度のデータをコピー
    function &getCopyModel() {

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //今年度と去年のデータ存在チェック
        $query = knjz092kQuery::CheckData($this->year);
        $konNendo = $db->getOne($query);
        $query = knjz092kQuery::CheckData($this->year - 1);
        $kyoNen = $db->getOne($query);

        if ($konNendo > 0) {
            $this->setWarning('MSG203', "今年度のデータが存在します。");
            return false;
        } elseif ($kyoNen == 0) {
            $this->setWarning('MSG203', "前年データが存在しません。");
            return false;
        } else{
            $query = knjz092kQuery::getCopyQuery($this->year);
            $db->query($query);
            $this->setMessage("MSG201");
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //変数をクリアします
    function clean() {}
}
?>

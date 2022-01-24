<?php

require_once('for_php7.php');
class knjp150kModel extends Model {
    var $field = array();
    var $cmd;
    var $year;
    var $schregno;
    var $exp_lcd;
    var $exp_mcd;
    var $exp_scd;
    var $titlecd;
    var $exp_sname;
    var $paid_input_flg;
    var $inst_cd;
    var $sex;
    var $disabled1;
    var $disabled2;
    var $disabled3;
    var $div;
    var $selectdata;
    var $repayFlgOnly;

    function knjp150kModel()
    {
    }

    function init()
    {
        if (VARS::get("PROGRAMID")!="") {
            unset($this->schregno);
            unset($this->exp_lcd);
            unset($this->exp_mcd);
            unset($this->exp_scd);
            unset($this->titlecd);
            unset($this->exp_sname);
            unset($this->paid_input_flg);
            unset($this->inst_cd);
            unset($this->hrclass);
            unset($this->sex);
        }
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if(strlen(VARS::get("SCHREGNO"))){
            $this->schregno = VARS::get("SCHREGNO");
            unset($this->exp_lcd);
            unset($this->exp_mcd);
            unset($this->exp_scd);
            unset($this->titlecd);
            unset($this->exp_sname);
            unset($this->paid_input_flg);
            unset($this->inst_cd);
        }

        if(VARS::get("init") != ""){
            unset($this->schregno);
            unset($this->titlecd);
            unset($this->exp_sname);
            unset($this->inst_cd);
        }

        if(strlen(VARS::get("EXPENSE_S_CD"))){
            $this->exp_lcd          = VARS::get("EXPENSE_L_CD");
            $this->exp_mcd          = VARS::get("EXPENSE_M_CD");
            $this->exp_scd          = VARS::get("EXPENSE_S_CD");
            $this->titlecd          = VARS::get("TITLECD");
            $this->exp_sname        = VARS::get("EXPENSE_S_NAME");
            $this->paid_input_flg   = VARS::get("PAID_INPUT_FLG");
            $this->inst_cd          = VARS::get("INST_CD");
        }

        $this->cmd  = VARS::request("cmd");
        $this->year = CTRL_YEAR;
        $this->div  = VARS::request("radiodiv");

        $this->field = array("PAID_MONEY"           => VARS::post("PAID_MONEY"),          //入金額
                             "PAID_MONEY_DATE"      => VARS::post("PAID_MONEY_DATE"),     //入金日
                             "PAID_MONEY_DIV"       => VARS::post("PAID_MONEY_DIV"),      //入金区分
                             "REPAY_MONEY"          => VARS::post("REPAY_MONEY"),         //返金額
                             "REPAY_SEQ"            => VARS::post("REPAY_SEQ"),           //返金回数
                             "REPAY_MONEY_DATE"     => VARS::post("REPAY_MONEY_DATE"),    //日
                             "REPAY_MONEY_DIV"      => VARS::post("REPAY_MONEY_DIV"),     //返金区分
                             "REPAY_FLG"            => VARS::post("REPAY_FLG"),           //返金済
                             "REMARK"               => VARS::post("REMARK"),              //備考
                             "TMP_PAID_MONEY"       => VARS::post("TMP_PAID_MONEY"),
                             "TMP_PAID_MONEY_DATE"  => VARS::post("TMP_PAID_MONEY_DATE"),
                             "TMP_PAID_MONEY_DIV"   => VARS::post("TMP_PAID_MONEY_DIV"),
                             "TMP_REPAY_MONEY"      => VARS::post("TMP_REPAY_MONEY"),
                             "TMP_REPAY_MONEY_DATE" => VARS::post("TMP_REPAY_MONEY_DATE"),
                             "TMP_REPAY_MONEY_DIV"  => VARS::post("TMP_REPAY_MONEY_DIV"),
                             "TMP_REPAY_FLG"        => VARS::post("TMP_REPAY_FLG"),
                             "TMP_REMARK"           => VARS::post("TMP_REMARK"),
                             "TOTALCD"              => VARS::post("TOTALCD"),
                             "EXPENSE_L_CD"         => substr(VARS::post("TOTALCD"),0,2),
                             "EXPENSE_M_CD"         => substr(VARS::post("TOTALCD"),2,2),
                             "EXPENSE_S_CD"         => substr(VARS::post("TOTALCD"),4,2),
                             "CHECKED1"             => VARS::post("CHECKED1"),
                             "CHECKED2"             => VARS::post("CHECKED2"),
                             "CHECKED3"             => VARS::post("CHECKED3")
                             );

        if (VARS::post("HRCLASS") != "") {
            $this->hrclass = VARS::post("HRCLASS");
        }
        if (VARS::post("SEX") != "") {
            $this->sex = VARS::post("SEX");
        }

        $this->selectdata = VARS::post("selectdata");
    }

    function validateInput($db, $schregno)
    {
        if ($this->cmd != "all_update") {
            if (!strlen($schregno)) {
                $this->warning = "左の生徒一覧から生徒を選択してください";
                return false;
            }
            if (!strlen($this->exp_scd)) {
                $this->setWarning("MSG304");
                return false;
            }
        } else {
            if (!strlen($this->field["EXPENSE_S_CD"])) {
                $this->setWarning("MSG304");
                return false;
            }
        }
        //バイト数チェック
        if (strlen($this->field["REMARK"]) > 30) {
            $this->setWarning("MSG901", "（備考 30バイトまで）");
            return false;
        } 

        if ($this->cmd != "all_update") {
            $totalRepayMoney = $db->getOne(knjp150kQuery::getTotalRePay($this, $this->year, $schregno, $this->exp_mcd, $this->exp_scd, "CHECK"));
            $paid_money         = (!strlen($this->disabled1)) ? $this->field["PAID_MONEY"] : $this->field["TMP_PAID_MONEY"];
            $paid_money_date    = (!strlen($this->disabled1)) ? $this->field["PAID_MONEY_DATE"] : $this->field["TMP_PAID_MONEY_DATE"];
            $paid_money_div     = (!strlen($this->disabled1)) ? $this->field["PAID_MONEY_DIV"] : $this->field["TMP_PAID_MONEY_DIV"];
            $repay_money        = (!strlen($this->disabled2)) ? $this->field["REPAY_MONEY"] : $this->field["TMP_REPAY_MONEY"];
            $totalRepayMoney = $repay_money + $totalRepayMoney;
            $repay_money_date   = (!strlen($this->disabled2)) ? $this->field["REPAY_MONEY_DATE"] : $this->field["TMP_REPAY_MONEY_DATE"];
            $repay_money_div    = (!strlen($this->disabled2)) ? $this->field["REPAY_MONEY_DIV"] : $this->field["TMP_REPAY_MONEY_DIV"];
            $remark             = (!strlen($this->disabled2)) ? $this->field["REMARK"] : $this->field["TMP_REMARK"];
            $repay_flg          = (!strlen($this->disabled3)) ? $this->field["REPAY_FLG"] : $this->field["TMP_REPAY_FLG"];
        } else {
            $totalRepayMoney = $db->getOne(knjp150kQuery::getTotalRePay($this, $this->year, $schregno, $this->field["EXPENSE_M_CD"], $this->field["EXPENSE_S_CD"], "CHECK"));
            $paid_money         = ($this->div != "1") ? $this->field["PAID_MONEY"] : $this->field["TMP_PAID_MONEY"];
            $paid_money_date    = ($this->div != "1") ? $this->field["PAID_MONEY_DATE"] : $this->field["TMP_PAID_MONEY_DATE"];
            $paid_money_div     = ($this->div != "1") ? $this->field["PAID_MONEY_DIV"] : $this->field["TMP_PAID_MONEY_DIV"];
            $repay_money        = $this->field["REPAY_MONEY"];
            $totalRepayMoney = $repay_money + $totalRepayMoney;
            $repay_money_date   = $this->field["REPAY_MONEY_DATE"];
            $repay_money_div    = $this->field["REPAY_MONEY_DIV"];
            $repay_flg          = $this->field["REPAY_FLG"];
        }
        //入金額と返金額の大小チェック
        if ((int)$paid_money < (int)$totalRepayMoney) {
            $this->setWarning("MSG901", "入金額、返金額");
            return;
        }
        //入金データ入力チェック
        if (strlen($paid_money) || strlen($paid_money_date) || strlen($paid_money_div)) {
            if (!strlen($paid_money) || !strlen($paid_money_date) || !strlen($paid_money_div)) {
                if (!strlen($paid_money)) {
                    $msg = "入金額";
                }
                if (!strlen($paid_money_date)) {
                    $msg = (strlen($msg)) ? $msg."、入金日" : "入金日";
                }
                if (!strlen($paid_money_div)) {
                    $msg = (strlen($msg)) ? $msg."、入金区分" : "入金区分";
                }
                $this->setWarning("MSG301", $msg);
                return;
            }
        }
        //返金金データ入力チェック
        if ($this->cmd != "all_update") {
            if (strlen($repay_money) || strlen($repay_money_date) || strlen($repay_money_div) || $repay_flg == "1") {
                if (!strlen($repay_money) || !strlen($repay_money_date) || !strlen($repay_money_div)) {
                    if (!strlen($repay_money)) {
                        $msg = "返金額";
                    }
                    if (!strlen($repay_money_date)) {
                        $msg = (strlen($msg)) ? $msg."、返金日" : "返金日";
                    }
                    if (!strlen($repay_money_div)) {
                        $msg = (strlen($msg)) ? $msg."、返金区分" : "返金区分";
                    }
                    $this->setWarning("MSG301", $msg);
                    return;
                }
            }
        } else {
            $this->repayFlgOnly = false;
            if (strlen($repay_money) || strlen($repay_money_date) || strlen($repay_money_div)) {
                if (!strlen($repay_money) || !strlen($repay_money_date) || !strlen($repay_money_div)) {
                    if (!strlen($repay_money)) {
                        $msg = "返金額";
                    }
                    if (!strlen($repay_money_date)) {
                        $msg = (strlen($msg)) ? $msg."、返金日" : "返金日";
                    }
                    if (!strlen($repay_money_div)) {
                        $msg = (strlen($msg)) ? $msg."、返金区分" : "返金区分";
                    }
                    $this->setWarning("MSG301", $msg);
                    return;
                }
            } else {
                $this->repayFlgOnly = true;
            }
        }
        //入金項目、返金項目、備考の全てが空の場合
        if (!strlen($paid_money) && !strlen($paid_money_date) && !strlen($paid_money_div) && !strlen($repay_money) &&
            !strlen($repay_money_date) && !strlen($repay_money_div) && !strlen($repay_flg)) {

            //入金済み小分類データ、中分類データを削除
            if ($this->cmd != "all_update")  {
                $this->getDeleteModel();
            }
        }
        return true;
    }

    function &getUpdateModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        //入力チェック
        if ($this->validateInput($db, $this->schregno)){

            $this->paid_input_flg = (strlen($this->paid_input_flg)) ? $this->paid_input_flg : "2";

            $repay_money        = (!strlen($this->disabled2)) ? $this->field["REPAY_MONEY"] : $this->field["TMP_REPAY_MONEY"];
            $repay_money_date   = (!strlen($this->disabled2)) ? $this->field["REPAY_MONEY_DATE"] : $this->field["TMP_REPAY_MONEY_DATE"];
            $repay_money_div    = (!strlen($this->disabled2)) ? $this->field["REPAY_MONEY_DIV"] : $this->field["TMP_REPAY_MONEY_DIV"];
            $repay_flg          = (!strlen($this->disabled3)) ? $this->field["REPAY_FLG"] : $this->field["TMP_REPAY_FLG"];

            if ($this->field["REPAY_SEQ"] &&
                !strlen($repay_money) && !strlen($repay_money_date) &&
                !strlen($repay_money_div) && !strlen($repay_flg)
            ) {
                //入金済み小分類データを削除(直近)
                $row["YEAR"] = $this->year;
                $row["SCHREGNO"] = $this->schregno;
                $row["REPAY_SEQ"] = $this->field["REPAY_SEQ"] ? $this->field["REPAY_SEQ"] : "-1";
                $delQuery = knjp150kQuery::getDeleteRePaySiteiQuery($row);
                $db->query($delQuery);
            } else {
                //小分類返金詳細データが存在するか確認
                $exist = $db->getOne(knjp150kQuery::ExistRePay($this, $this->year, $this->schregno));

                if (!strlen($exist) && $this->field["REPAY_MONEY"]) {
                    $query = knjp150kQuery::getInsertRePayQuery($db, $this, $this->schregno);
                    $db->query($query);
                } else {
                    $query = knjp150kQuery::getUpdateRePayQuery($db, $this, $this->schregno);
                    $db->query($query);
                }
            }

            //入金済み小分類データが存在するか確認
            $exist = $db->getOne(knjp150kQuery::ExistData($this->year, $this->schregno, $this->exp_mcd, $this->exp_scd, $this->paid_input_flg));

            if (!strlen($exist)) {
                $query = knjp150kQuery::getInsertQuery($db, $this, $this->schregno);
                $db->query($query);
            } else {
                $query = knjp150kQuery::getUpdateQuery($db, $this, $this->schregno);
                $db->query($query);
            }

            $row = $db->getRow(knjp150kQuery::getPaidSdata($this->year, $this->schregno, $this->exp_mcd, $this->paid_input_flg), DB_FETCHMODE_ASSOC);

            //入金済み中分類データが存在するか確認
            $exist = $db->getOne(knjp150kQuery::ExistData2($this->year, $this->schregno, $this->exp_lcd, $this->exp_mcd, $this->paid_input_flg));

            if (!strlen($exist)) {
                $query = knjp150kQuery::getInsertQuery2($this, $this->schregno, $row);
                $db->query($query);
            } else {
                $query = knjp150kQuery::getUpdateQuery2($this, $this->schregno, $row);
                $db->query($query);
            }

        }
        $db->commit();
        Query::dbCheckIn($db);
        $this->setMessage("MSG201");
    }

    function &getDeleteModel()
    {
        $this->paid_input_flg = (strlen($this->paid_input_flg)) ? $this->paid_input_flg : "2";

        if($this->paid_input_flg != "1") {
            $db = Query::dbCheckOut();   
            $db->autoCommit(false);

            //入金済み小分類データの削除
            $query = knjp150kQuery::getDeleteQuery($this, $this->schregno);
            $db->query($query);

            //入金済み小分類データの削除
            $query = knjp150kQuery::getDeleteRePayQuery($this, $this->schregno);
            $db->query($query);

            $row = $db->getRow(knjp150kQuery::Paid_S_Data($this, $this->schregno));

            if (!is_array($row)) {
                //入金済み中分類データの削除
                $query = knjp150kQuery::getDeleteQuery2($this, $this->schregno);
                $db->query($query);
            } else {
                $row = $db->getRow(knjp150kQuery::getPaidSdata($this->year, $this->schregno, $this->exp_mcd, $this->paid_input_flg), DB_FETCHMODE_ASSOC);

                //入金済み中分類データが存在するか確認
                $exist = $db->getOne(knjp150kQuery::ExistData2($this->year, $this->schregno, $this->exp_lcd, $this->exp_mcd, $this->paid_input_flg));

                if (!strlen($exist)) {
                    $query = knjp150kQuery::getInsertQuery2($this, $this->schregno, $row);
                    $db->query($query);
                } else {
                    $query = knjp150kQuery::getUpdateQuery2($this, $this->schregno, $row);
                    $db->query($query);
                }
            }
            $db->commit();
            Query::dbCheckIn($db);
        }
        $this->setMessage("MSG201");
    }

    //一括更新
    function &getAllUpdateModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $data = explode(",", $this->selectdata);
        foreach ($data as $schregno) {
            if (!$this->validateInput($db, $schregno)) {
                return;
            }
        }

        $backUpDiv = $this->div;
        foreach ($data as $schregno) {
            //生徒毎に入金データの入金区分（CSV,窓口）を取得する。
            $this->div = $backUpDiv;
            $paidDiv = $db->getOne(knjp150kQuery::getPaidDiv($this, $schregno));
            $this->div = $paidDiv ? $paidDiv : $this->div;

            //入金予定データチェック
            $exist = $db->getOne(knjp150kQuery::MoneyDueData($this, $schregno));
            if (!strlen($exist)) {
                $this->setWarning("MSG203", "入金予定データに存在しません。\\n\\n学籍番号：".$schregno);
                Query::dbCheckIn($db);
                return;
            }
            //分納データチェック
            $exist = $db->getOne(knjp150kQuery::MoneyInstData($this, $schregno));
            if (strlen($exist)) {
                $this->setWarning("MSG203", "分納データが存在します。\\n\\n学籍番号：".$schregno);
                Query::dbCheckIn($db);
                return;
            }

            if (strlen($this->field["CHECKED2"]) && !strlen($this->field["REPAY_MONEY"]) &&
                !strlen($this->field["REPAY_MONEY_DATE"]) && !strlen($this->field["REPAY_MONEY_DIV"]) &&
                !strlen($this->field["REPAY_FLG"])
            ) {
                //入金済み小分類データを削除(直近)
                $this->getRepayDeleteModel($db, $schregno);
            }

            if (strlen($this->field["CHECKED2"]) && !$this->repayFlgOnly) {
                //小分類返金詳細データが存在するか確認
                $exist = $db->getOne(knjp150kQuery::ExistRePay($this, $this->year, $schregno));

                if (!strlen($exist)) {
                    $query = knjp150kQuery::getInsertRePayQuery($db, $this, $schregno);
                    $db->query($query);
                } else {
                    $query = knjp150kQuery::getUpdateRePayQuery($db, $this, $schregno);
                    $db->query($query);
                }
            }

            //入金済み小分類データが存在するか確認
            $exist = $db->getOne(knjp150kQuery::ExistData($this->year, $schregno, $this->field["EXPENSE_M_CD"], $this->field["EXPENSE_S_CD"], $this->div));
            if (!strlen($exist)) {
                if (!strlen($this->field["CHECKED1"]) &&
                    (strlen($this->field["CHECKED2"]) && !$this->repayFlgOnly)
                ) {
                    //データ追加の場合に返金情報が選択されていて、入金情報が選択されていない場合
                    $this->setWarning("MSG901", "入金額、返金額\\n\\n学籍番号：".$schregno);
                    Query::dbCheckIn($db);
                    return; 
                }
                $query = knjp150kQuery::getInsertQuery($db, $this, $schregno);
                $db->query($query);
            } else {
                $query = knjp150kQuery::getUpdateQuery($db, $this, $schregno);
                $db->query($query);
            }

            $row = $db->getRow(knjp150kQuery::getPaidSdata($this->year, $schregno, $this->field["EXPENSE_M_CD"], $this->div), DB_FETCHMODE_ASSOC);

            //入金済み中分類データが存在するか確認
            $exist = $db->getOne(knjp150kQuery::ExistData2($this->year, $schregno, $this->field["EXPENSE_L_CD"], $this->field["EXPENSE_M_CD"], $this->div));

            if (!strlen($exist)) {
                $query = knjp150kQuery::getInsertQuery2($this, $schregno, $row);
                $db->query($query);
            } else {
                $query = knjp150kQuery::getUpdateQuery2($this, $schregno, $row);
                $db->query($query);
            }
        }

        $this->div = $backUpDiv;
        $db->commit();
        $this->setMessage("MSG201");
        Query::dbCheckIn($db);
    }

    //直近削除
    function getRepayDeleteModel($db, $schregno) {
        $query = knjp150kQuery::getMaxRePayKey($this, $this->year, $schregno, $this->field["EXPENSE_M_CD"], $this->field["EXPENSE_S_CD"]);
        $query .= "SELECT * FROM MAX_DATA";
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $delQuery = knjp150kQuery::getDeleteRePaySiteiQuery($row);
            $db->query($delQuery);
        }
    }

    //一括削除
    function &getAllDeleteModel()
    {
        if ($this->div != "1") {
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            $data = explode(",", $this->selectdata);

            foreach ($data as $schregno)
            {
                //生徒毎に入金データの入金区分（CSV,窓口）を取得する。
                $paidDiv = $db->getOne(knjp150kQuery::getPaidDiv($this, $schregno));
                $this->div = $paidDiv ? $paidDiv : $this->div;

                //入金予定データチェック
                $exist = $db->getOne(knjp150kQuery::MoneyDueData($this, $schregno));
                if (!strlen($exist)) {
                    $this->setWarning("MSG203", "入金予定データに存在しません。\\n\\n学籍番号：".$schregno);
                    Query::dbCheckIn($db);
                    return;
                }
                //分納データチェック
                $exist = $db->getOne(knjp150kQuery::MoneyInstData($this, $schregno));
                if (strlen($exist)) {
                    $this->setWarning("MSG203", "分納データが存在します。\\n\\n学籍番号：".$schregno);
                    Query::dbCheckIn($db);
                    return;
                }

                //入金済み小分類データの削除
                $query = knjp150kQuery::getDeleteRePayQuery($this, $schregno);
                $db->query($query);
                //入金済み小分類データの削除
                $query = knjp150kQuery::getDeleteQuery($this, $schregno);
                $db->query($query);

                $row = $db->getRow(knjp150kQuery::Paid_S_Data($this, $schregno));

                if (!is_array($row)) {
                    //入金済み中分類データの削除
                    $query = knjp150kQuery::getDeleteQuery2($this, $schregno);
                    $db->query($query);
                } else {
                    $row = $db->getRow(knjp150kQuery::getPaidSdata($this->year, $schregno, $this->field["EXPENSE_M_CD"], $this->div), DB_FETCHMODE_ASSOC);

                    //入金済み中分類データが存在するか確認
                    $exist = $db->getOne(knjp150kQuery::ExistData2($this->year, $schregno, $this->field["EXPENSE_L_CD"], $this->field["EXPENSE_M_CD"], $this->div));

                    if (!strlen($exist)) {
                        $query = knjp150kQuery::getInsertQuery2($this, $schregno, $row);
                        $db->query($query);
                    } else {
                        $query = knjp150kQuery::getUpdateQuery2($this, $schregno, $row);
                        $db->query($query);
                    }
                }
            }
            $db->commit();
            Query::dbCheckIn($db);
        }
        $this->setMessage("MSG201");
    }
}
?>

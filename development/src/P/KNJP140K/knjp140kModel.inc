<?php

require_once('for_php7.php');

class knjp140kModel extends Model {
    var $field      = array();
    var $cmd;
    var $schregno;
    var $sum;		//NO002
    var $sum2;		//NO002
    var $sum3;		//NO002
	var $money2;	//NO003
    var $control_data;

    function knjp140kModel()
    {
        common::GetControlMaster_Fnc($this->control_data);
        $this->div = "M";
    }

    function init()
    {
        $this->cmd = VARS::request("cmd");

        $this->name     = VARS::get("name");
        $this->div      = VARS::request("radiodiv");

        $this->field = array("INST_DUE_DATE"   => VARS::post("INST_DUE_DATE"),
                             "INST_MONEY_DUE"  => VARS::post("INST_MONEY_DUE"),
                             "PAID_MONEY"      => VARS::post("PAID_MONEY"),
                             "PAID_MONEY_DIV"  => VARS::post("PAID_MONEY_DIV"),
                             "PAID_MONEY_DATE" => VARS::post("PAID_MONEY_DATE"),
                             "REPAY_MONEY"     => VARS::post("REPAY_MONEY"),
                             "REPAY_DATE"      => VARS::post("REPAY_DATE"),
                             "REMARK"          => VARS::post("REMARK"));

        if (VARS::get("INST_CD")!="") {
            $this->inst_cd = VARS::get("INST_CD");
        }
        if (VARS::get("INST_SEQ")!="") {
            $this->inst_seq = VARS::get("INST_SEQ");
        }

        $this->selectdata = VARS::post("selectdata");

        //変数初期化
        if(VARS::get("SCHREGNO") != "" || VARS::get("init") || VARS::get("PROGRAMID")) {
            $this->schregno = VARS::get("SCHREGNO");
            $this->div = "M";
            unset($this->field);
            unset($this->inst_cd);
            unset($this->inst_seq);
        }
    }

    function validateInput()
    {
        if ($this->schregno == "") {
            $this->warning = "左の生徒一覧から生徒を選択してください";
            return false;
        }

        if (strlen($this->field["REMARK"]) > 75) {
            $this->setWarning("MSG901", "（備考 75バイトまで）");
            return false;
        } 
        return true;
    }

    //分納対象費目編集の追加と更新
    function &getUpdate1Model()
    {
        if ($this->schregno == "") {
            $this->warning = "左の生徒一覧から生徒を選択してください";
            return false;
        }

        if (knjp140kQuery::getUpdate1Query($this)) {
            $this->setWarning("MSG201");
        }
    }



    //分納の期限と金額の追加 or 更新
    function &getUpdateModel()
    {
        //入力チェック
        if (!$this->validateInput())
            return;

        if ($this->cmd == "update" && $this->inst_seq == "") {
            $this->setWarning("MSG308");
            return false;
        }

        //入金データチェック
        if (strlen($this->field["PAID_MONEY"]) || strlen($this->field["PAID_MONEY_DIV"]) || strlen($this->field["PAID_MONEY_DATE"])) {
            if (!strlen($this->field["PAID_MONEY"]) || !strlen($this->field["PAID_MONEY_DIV"]) || !strlen($this->field["PAID_MONEY_DATE"])) {
                if (!strlen($this->field["PAID_MONEY"])) {
                    $msg = "入金額";
                }
                if (!strlen($this->field["PAID_MONEY_DIV"])) {
                    $msg = (strlen($msg)) ? $msg."、入金区分" : "入金区分";
                }
                if (!strlen($this->field["PAID_MONEY_DATE"])) {
                    $msg = (strlen($msg)) ? $msg."、入金日" : "入金日";
                }
                $this->setWarning("MSG301", $msg);
                return;
            }
        }

        //入金額と返金額の大小チェック
        if ((int)$this->field["PAID_MONEY"] < (int)$this->field["REPAY_MONEY"]) {
            $this->setWarning("MSG901", "返金額が入金額を超えています。");
            return;
        } 

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //入金済みデータに返金情報があるかチェック
        $query = knjp140kQuery::CheckPaidData($this->schregno, $this->inst_cd, 2);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        if (is_array($row)) {
            $this->setWarning("MSG203","入金済データに返金情報があるので更新できません。");
            Query::dbCheckIn($db);
            return false;
        }

        $query = knjp140kQuery::ExistCheck($this);
        $exist_arr = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //分納期限重複チェック
        if (is_array($exist_arr)) {
            $this->setWarning("MSG302", "分納期限");
            Query::dbCheckIn($db);
            return;
        }

		//NO002-->
        //更新した金額が不正かチェック
        $row = $db->getRow(knjp140kQuery::GetSumMoney($this->schregno, $this->inst_cd), DB_FETCHMODE_ASSOC);
        if ((int)$row["MONEY_DUE"] <
			((int)$row["PAID_MONEY"] + (int)$this->field["PAID_MONEY"]) -
			((int)$row["REPAY_MONEY"] + (int)$this->field["REPAY_MONEY"])) {
            $this->setWarning("MSG901", "納入必要金額を超えています。");
            return;
        }
		//NO002<--

        //追加 or 更新
        if ($this->cmd == "add") {
            //既存データのINST_SEQの最大値＋１を新規データのSEQにする
            $this->inst_seq = $db->getOne(knjp140kQuery::GetNewSeq($this->schregno, $this->inst_cd));
            if (!strlen($this->inst_seq)) {
                $this->inst_seq = "1";
            }
            //追加処理
            $query = knjp140kQuery::getAddQuery($this);
            $db->query($query);
        } else {
            //更新処理
            $query = knjp140kQuery::getUpdateQuery($this);
            $db->query($query);
        }
        
        //更新した金額が不正かチェック
        $row = $db->getRow(knjp140kQuery::GetSumMoney($this->schregno, $this->inst_cd), DB_FETCHMODE_ASSOC);
        if ((int)$row["MONEY_DUE"] > (int)$this->money[$this->inst_cd]["MONEY_DUE"]) {
            $this->setWarning("MSG901", "納入必要金額を超えています。");
            $db->rollback();
            Query::dbCheckIn($db);
            return;
        }
        $msg = "";
        //過剰入金チェック
        if (((int)$row["PAID_MONEY"] - (int)$row["REPAY_MONEY"]) > (int)$this->money[$this->inst_cd]["MONEY_DUE"]) {
            $msg = "入金額が納入必要金額を超えています。";
        } 

        
        //入金額と納入必要金額が一致しているか
        if (((int)$row["PAID_MONEY"] - (int)$row["REPAY_MONEY"]) == (int)$this->money[$this->inst_cd]["MONEY_DUE"]) {
            //入金済データを作成
            knjp140kQuery::InsertPaidMoney($db, $this->schregno, $this->inst_cd);
            $msg = "入金完了しました。";
        } else {
            //入金済みデータ取消処理
            $flg = knjp140kQuery::PaidMoneyClear($db, $this->schregno, $this->inst_cd);

            if ($flg == true) {
                $msg = (strlen($msg)) ? $msg."\\n入金完了を取消ました。" : "入金完了を取消ました。";
            }
        }
            

        //分納納入期限の昇順で並び替え
        $this->SortInstSeq($db);

        //新しく設定された分納SEQを取得
        $this->inst_seq = $db->getOne(knjp140kQuery::GetNewSeq2($this));
        $db->commit();
        $this->setMessage("MSG201", $msg);
        Query::dbCheckIn($db);
    }

    //分納の期限と金額削除
    function &getDeleteModel()
    {
        if ($this->schregno == "") {
            $this->warning = "左の生徒一覧から生徒を選択してください";
            return false;
        }

        if ($this->inst_seq == "") {
            $this->setWarning("MSG308");
            return false;
        }

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //入金済みデータに返金情報があるかチェック
        $query = knjp140kQuery::CheckPaidData($this->schregno, $this->inst_cd, 2);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        if (is_array($row)) {
            $this->setWarning("MSG203","入金済データに返金情報があるので更新できません。");
            Query::dbCheckIn($db);
            return false;
        }

        //データ削除
        $query = knjp140kQuery::getDeleteQuery($this);
        $db->query($query);

        $row = $db->getRow(knjp140kQuery::GetSumMoney($this->schregno, $this->inst_cd), DB_FETCHMODE_ASSOC);

        $msg = "";
        //入金額と納入必要金額が一致しているか
        if (((int)$row["PAID_MONEY"] - (int)$row["REPAY_MONEY"]) == (int)$this->money[$this->inst_cd]["MONEY_DUE"]) {
            //入金済データを作成
            knjp140kQuery::InsertPaidMoney($db, $this->schregno, $this->inst_cd);
            $msg = "入金完了しました。";
        } else {
            //入金済みデータ取消処理
            $flg = knjp140kQuery::PaidMoneyClear($db, $this->schregno, $this->inst_cd);

            if ($flg == true) {
                $msg = "入金完了を取消ました。";
            }
        }

        //分納納入期限の昇順で並び替え
        $this->SortInstSeq($db);

        $this->inst_seq = "";
        unset($this->field);

        $db->commit();
        $this->setMessage("MSG201", $msg);
        Query::dbCheckIn($db);
    }


    //分納納入期限の昇順で並び替え
    function SortInstSeq($db)
    {
        //分納納入期限の昇順でデータ取得
        $result = $db->query(knjp140kQuery::getInstData($this->schregno, $this->inst_cd));
        $i = 1;
        $tmp_arr = array();

        //配列にデータ格納
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $row["INST_SEQ"] = $i;
            $tmp_arr[] = $row;
            
            $i++;
        }

        //データ削除
        $db->query(knjp140kQuery::getDeleteQuery2($this));

        //分納納入期限の昇順で並び替えた配列でデータ追加
        for ($j=0; $j<get_count($tmp_arr); $j++) {
            
            $query = knjp140kQuery::getAddQuery2($tmp_arr[$j]);
            $db->query($query);
        }
        return;
    }
}
?>

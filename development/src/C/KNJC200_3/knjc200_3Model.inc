<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjc200_3Model extends Model {
    var $field      = array();
    var $cmd;
	var $control;
    var $hr_name;
    var $s_period;
    var $e_period;
    var $arr_period;

    /**
     * メンバ変数の初期化
     */
    function knjc200_3Model()
    {
		//コントロールマスタより情報を取得
		common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("SCHREGNO"			=>  VARS::post("SCHREGNO"),		    //生徒
							 "SEQNO"			=>  VARS::post("SEQNO"),		    //直近データ
							 "CONTACTER"		=>	VARS::post("CONTACTER"),	    //連絡元
							 "FROMDATE"		    =>	VARS::post("FROMDATE"),	        //開始日付
							 "TODATE"		    =>	VARS::post("TODATE"),	        //終了日付
							 "FROMPERIOD"		=>	VARS::post("FROMPERIOD"),	    //開始校時
							 "TOPERIOD"		    =>	VARS::post("TOPERIOD"),	        //終了校時
							 "CALLBACK"		    =>	VARS::post("CALLBACK"),	        //返電
							 "DI_CD"		    =>	VARS::post("DI_CD"),	        //勤怠
							 "DI_REMARK"		=>	VARS::post("DI_REMARK"),	    //理由(備考)
							 "SONOTA"		    =>	VARS::post("SONOTA"),	        //その他
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        if (VARS::get("hr_name")) $this->hr_name = VARS::get("hr_name");

        $this->cmd         = VARS::request("cmd");
    }

    //入力項目チェック
    function validateInput()
    {
        //キーの必須選択チェック
        if ($this->field["SCHREGNO"] == "") {
            $this->setWarning("MSG310", "(生徒)");
            return false;
        }
        if ($this->field["FROMDATE"] == "") {
            $this->setWarning("MSG301", "(開始日付)");
            return false;
        }
        if ($this->field["TODATE"] == "") {
            $this->setWarning("MSG301", "(終了日付)");
            return false;
        }
        //年度内チェック
        $sdate = CTRL_YEAR ."/04/01";
        $edate = CTRL_YEAR + 1 ."/03/31";
        if (!($sdate <= $this->field["FROMDATE"]) || !($this->field["FROMDATE"] <= $edate)) {
            $this->warning = "処理年度範囲外です。(開始日付)";
            return false;
        }
        if (!($sdate <= $this->field["TODATE"]) || !($this->field["TODATE"] <= $edate)) {
            $this->warning = "処理年度範囲外です。(終了日付)";
            return false;
        }
        //文字数チェック
        if (strlen($this->field["SONOTA"]) > 30 ) {
            $this->setWarning("MSG915", "(その他) 30バイトまでです。");
            return false;
        }
        //大小チェック
        if ($this->field["TODATE"] < $this->field["FROMDATE"]) {
            $this->warning = "開始日付が終了日付を超えています。";
            return false;
        }
        if ($this->field["FROMPERIOD"] == "")   $this->field["FROMPERIOD"]  = $this->s_period;
        if ($this->field["TOPERIOD"] == "")     $this->field["TOPERIOD"]    = $this->e_period;
        if (($this->field["TODATE"] == $this->field["FROMDATE"]) && ($this->field["TOPERIOD"] < $this->field["FROMPERIOD"])) {
            $this->warning = "開始校時が終了校時を超えています。";
            return false;
        }

        return true;
    }

    //追加および更新
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
        }
    }

    //UPDATE（直近データが「なし」の場合、追加。「なし」以外の場合、更新。）
    function &update()
    {
        if ($this->field["SEQNO"] == "") {
            $ret_seqno = knjc200_3Query::getMaxSeqno();
            $this->field["SEQNO"] = $ret_seqno;//受付番号Max値をセット
            knjc200_3Query::getInsertQuery($this);
            $this->message = "データを新規作成しました。";
        } else {
            knjc200_3Query::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //DELETE
    function &getDeleteModel()
    {
        //キーの必須選択チェック
        if ($this->field["SCHREGNO"] == "") {
            $this->setWarning("MSG308", "(生徒)");
            return false;
        }
        if ($this->field["SEQNO"] == "") {
            $this->setWarning("MSG310", "(この生徒の登録済みデータ)");
            return false;
        }

        $ret_val = knjc200_3Query::getDeleteQuery($this);
        unset($this->field["SEQNO"]);
        $this->message = "データを削除しました。";
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>

<?php

require_once('for_php7.php');

class knjg041Model extends Model {

    var $cmd;                        //cmd
    var $apply_div;                  //申請区分コンボ
    var $apply_data = array();       //申請区分データ
    var $perm_data  = array();       //許可区分データ

    var $perm_div;                   //申請区分コンボ
    var $perm_cd;                    //申請区分
    var $field      = array();       //エレメント値
    var $backup_dt  = array();       //フィールドのバックアップ
    var $clear;                      //値クリアフラグ
    var $control_data = array();     //コントロールマスタデータを保持
    var $sec_competence;             //権限

    /**
     * メンバ変数の初期化
     */
    function knjg041Model()
    {    

    }
    
    function init() {
        if (VARS::get("PROGRAMID")) {
            //学籍処理日取得
            common::GetControlMaster_Fnc($this->control_data);
            //名称マスタより申請区分＆許可区分のデータを取得
            common::GetNameMaster("G100","NAME1","",$this->apply_data, CTRL_YEAR);
            common::GetNameMaster("G101","NAME1","",$this->perm_data, CTRL_YEAR);
            //セキュリティーチェック
            $this->sec_competence = common::SecurityCheck(STAFFCD,"KNJG041");

            //申請区分初期化
            $this->apply_div = 1;

            //許可区分初期化
            $this->perm_div = 0;

            unset($this->backup_dt);
        }
        //期間開始日付
        if (VARS::post("sdate") && VARS::post("HOUR1") && VARS::post("MINUTE1")) {
            $sday = str_replace("/","-",VARS::post("sdate"))." ".VARS::post("HOUR1").":".VARS::post("MINUTE1").":00";
        } else {
            $sday = VARS::get("sdate");
        }

        //期間終了日付
        if (VARS::post("edate") && VARS::post("HOUR2") && VARS::post("MINUTE2")) {
            $eday = str_replace("/","-",VARS::post("edate"))." ".VARS::post("HOUR2").":".VARS::post("MINUTE2").":00";
        } else {
            $eday = VARS::get("edate");
        }

        //詳細情報
        $this->field = array("APPLYDAY"         =>  VARS::request("applyday"),      //申請日
                             "APPLYCD"          =>  VARS::request("APPLYCD"),          //申請区分
                             "SDATE"            =>  $sday,                          //期間開始日付
                             "EDATE"            =>  $eday,                          //期間終了日付

                             "HOUR1"            =>  VARS::post("HOUR1"),            //期間開始(時間)
                             "MINUTE1"          =>  VARS::post("MINUTE1"),          //期間開始(分)
                             "HOUR2"            =>  VARS::post("HOUR2"),            //期間終了(時間)
                             "MINUTE2"          =>  VARS::post("MINUTE2"),          //期間終了(分)

                             "HOURS"            =>  VARS::post("TOTAL_HOURS"),      //所要時
                             "MINUTES"          =>  VARS::post("TOTAL_MINUTES"),    //所要分
                             "VACATION"         =>  VARS::post("LOCATION"),         //休暇出張地
                             "VACATIONREASON"   =>  VARS::post("REASON"),           //休暇出張理由
                             "GUIDE"            =>  VARS::post("LEADING"),          //出張引率
                             "GUIDE_NUM"        =>  VARS::post("ST_NUM"),           //主張引率生徒数
                             "BUSINESSTRIP"     =>  VARS::post("BUSSI_TRIP"),       //出張
                             "REMARK"           =>  VARS::post("OTHERS"),           //備考
                             "CALL_NAME"        =>  VARS::post("CALL_NAME"),        //連絡先名称
                             "CALL_TELNO"       =>  VARS::post("CNTCT_TELNO"),      //連絡先電話番号
                             "PRINTAPPLYCD"     =>  VARS::post("PRINTAPPLYCD"),     //申請区分（印刷ボタン時）
                             "PRINTAPPLYDAY"    =>  VARS::post("PRINTAPPLYDAY"),    //申請日付（印刷ボタン時）
                             "PRINTPERMCD"      =>  VARS::post("PRINTPERMCD"),      //許可区分（印刷ボタン時）
                             "PRINTSTAFFCD"     =>  VARS::post("PRINTSTAFFCD"),     //職員コード（印刷ボタン時）
                             "PRINTSTARTDAY"    =>  VARS::post("PRINTSTARTDAY"),    //期間開始日（印刷ボタン時）
                             "PRINTENDDAY"      =>  VARS::post("PRINTENDDAY"),      //期間終了日（印刷ボタン時）
                             "DBNAME"           =>  VARS::post("DBNAME"),           //DB変更に備えて

                             "PERM_CD"          =>  VARS::request("PERM_CD"),       //許可区分
                             "PERM_STAFFCD"     =>  VARS::request("PERM_STAFFCD"),  //許可職員コード
                             "UPDATED"          =>  VARS::post("UPDATED")           //更新日付
                        );

        $this->clear        = VARS::post("clear");
        $this->cmd          = VARS::request("cmd");
        $this->apply_date   = VARS::get("APPLY");

        //バックアップ用値取得
        if (VARS::get("applyday") && VARS::get("APPLYCD") && VARS::get("sdate") && VARS::get("edate")) {

            $this->backup_dt = array("APPLYDAY" => VARS::get("applyday"),
                                     "APPLYCD"  => VARS::get("APPLYCD"),
                                     "SDATE"    => VARS::get("sdate"),
                                     "EDATE"    => VARS::get("edate")
                                    );
        }

        //申請区分値取得
        if (VARS::request("apply_div")) {
            $this->apply_div = VARS::request("apply_div");
        }

        //許可区分値取得
        if (VARS::request("perm_div") != "") {
            $this->perm_div = VARS::request("perm_div");
        }
    }

    //入力項目チェック
    function validateInput($type) {

        if (!$this->field["APPLYDAY"] || !$this->field["APPLYCD"] ||
           !$this->field["SDATE"] || !$this->field["EDATE"] || !$this->field["HOUR1"] ||
           !$this->field["MINUTE1"] || !$this->field["HOUR2"] || !$this->field["MINUTE2"] ||
           ($this->sec_competence == 2 && $this->field["PERM_CD"] == ""))
        {
            if ($type == "insert") {
                $this->setWarning("MSG301");
                return false;
            } else if ($type == "update" || $type == "delete") {
                $this->setWarning("MSG308");
                return false;
            }
        }

        //日時チェック
        if ($type == "update" || $type == "insert") {
            $sdate = array();
            $edate = array();
            $S = array();
            $F = array();
            $bad_flg = false;
            $bad_Msg = "日付 - ";

            $sdate = explode(" ",$this->field["SDATE"]);
            $edate = explode(" ",$this->field["EDATE"]);

            $S = explode("-",($sdate[0]));
            $F = explode("-",($edate[0]));

            //年
            if($S[0] == $F[0]) {
                //月
                if($S[1] == $F[1]) {
                    //日
                    if($S[2] == $F[2]) {
                        $S = explode(":",($sdate[1]));
                        $F = explode(":",($edate[1]));
                        //時
                        if ($S[0] == $F[0]) {
                            //分
                            if ($S[1] == $F[1]) {
                                $bad_flg = true;
                            } else if ($S[1] < $F[1]) {
                                $bad_flg = true;
                            } else {
                                $bad_Msg .= "分";
                            }
                        } else if ($S[0] < $F[0]) {
                            $bad_flg = true;
                        } else {
                            $bad_Msg .= "時";
                        }
                    } else if ($S[2] < $F[2]) {
                        $bad_flg = true;
                    } else {
                        $bad_Msg .= "日";
                    }
                } else if ($S[1] < $F[1]) {
                    $bad_flg .= true;
                } else{
                    $bad_Msg = "月";
                }
            } else if ($S[0] < $F[0]) {
                $bad_flg = true;
            } else {
                $bad_Msg .= "年";
            }

            //フラグがfalseならばreturn
            if (!$bad_flg) {
                $this->setWarning("MSG901",$bad_Msg);
                return false;
            }
        }

        //１レコード取得(更新前データ)
        $row = knjg041Query::getRow_data($this->backup_dt, STAFFCD);

        if ($type == "update" || $type == "delete") {
            
            if ($row["UPDATED"] != $this->field["UPDATED"]) {
                $this->setWarning("MSG307");
                return false;
            }
        }

        if ($type == "insert") {
            //１レコード取得(更新データ)
            $row_now = knjg041Query::getRow_data($this->field, STAFFCD);
            if (!is_null($row_now)) {
                $this->setWarning("MSG302");
                return false;
            }
        }

        //文字数チェック
        if ($type == "update" || $type == "insert") {
            if ($this->field["APPLYDAY"] == "" || !($this->field["APPLYDAY"])) {
                $this->setWarning("MSG301", "( 申請日 )");
                return false;
            } else if ($this->field["APPLYCD"] == "" ) {
                $this->setWarning("MSG310", "( 申請区分 )");
                return false;
            } else if ($this->field["SDATE"] == "" ) {
                $this->setWarning("MSG301", "( 期間開始日付 )");
                return false;
            } else if ($this->field["EDATE"] == "" ) {
                $this->setWarning("MSG301", "( 期間終了日付 )");
                return false;
            } else if (strlen($this->field["HOURS"]) > 2 ) {
                $this->setWarning("MSG915", "( 所要時 ) 2バイトまで ");
                return false;
            } else if (strlen($this->field["MINUTES"]) > 2 ) {
                $this->setWarning("MSG915", "( 所要分 ) 2バイトまで");
                return false;
            } else if (strlen($this->field["VACATION"]) > 372 ) {
                $this->setWarning("MSG915", "( 休暇出張地 ) 372バイトまで");
                return false;
            } else if (strlen($this->field["VACATIONREASON"]) > 372 ) {
                $this->setWarning("MSG915", "( 休暇出張理由 ) 372バイトまで");
                return false;
            } else if (strlen($this->field["GUIDE"]) > 93 ) {
                $this->setWarning("MSG915", "( 出張引率 ) 93バイトまで");
                return false;
            } else if (strlen($this->field["GUIDE_NUM"]) > 2 ) {
                $this->setWarning("MSG915", "( 主張引率生徒数 ) 2バイトまで");
                return false;
            } else if (strlen($this->field["BUSINESSTRIP"]) > 186 ) {
                $this->setWarning("MSG915", "( 出張 ) 186バイトまで");
                return false;
            } else if (strlen($this->field["REMARK"]) > 153 ) {
                $this->setWarning("MSG915", "( 備考 ) 153バイトまで");
                return false;
            } else if (strlen($this->field["CALL_NAME"]) > 30 ) {
                $this->setWarning("MSG915", "( 連絡先名称 ) 30バイトまで");
                return false;
            } else if (strlen($this->field["CALL_TELNO"]) > 14 ) {
                $this->setWarning("MSG915","( 連絡先電話番号 ) 14バイトまで");
                return false;
            }
        }
        return true;
    }

    /**
     * 編集完了用のモデルを返します
    */
    function &getUpdateModel() {
        //入力チェック
          if ($this->validateInput("update")) {
            if ($this->update()) {
                $this->data_Uniform();
                $this->setMessage("MSG201");
                return;
            }
        }
    }

    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput("insert")) {
            if ($this->insert()) {
                $this->data_Uniform();
                $this->setMessage("MSG201");
            }
        }
    }

    /**
     * 削除用のモデルを返します
     */
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput("delete")) {
            if ($this->delete()) {
                $this->setMessage("MSG201");
            }
        }
    }

    function &delete() {
        $result = knjg041Query::getDeleteQuery($this);
        return $result;
    }

    function &insert() {
        $result = knjg041Query::getInsertQuery($this);
        return $result;
    }

    function &update() {
        $result = knjg041Query::getUpdateQuery($this);
        return $result;
    }

    //以前のデータと更新したデータをあわせる。
    function data_Uniform() {
        $this->backup_dt = $this->field;
    }
}
?>

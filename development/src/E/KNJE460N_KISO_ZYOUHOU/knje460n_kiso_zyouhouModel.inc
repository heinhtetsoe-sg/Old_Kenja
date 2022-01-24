<?php

require_once('for_php7.php');

class knje460n_kiso_zyouhouModel extends Model {
    var $cmd;
    var $schregno = "";
    var $name = "";
    var $field = array();
    var $control;
    var $exp_year;      //左フレームから送られてくる年度
    var $exp_semester;  //左フレームから送られてくる学期
    var $getPrgId;      //コールされたか
    var $sendAuth;      //親画面の権限
    var $auth;          //権限
    var $grade;         //左フレームから送られてくる年組
    var $upddate;       //右フレームで設定する更新日

    var $kiso_zyouhou_moji;  //基礎情報の文字数
    var $kiso_zyouhou_gyou;  //基礎情報の行数
    var $target = "";

    function knje460n_kiso_zyouhouModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->upddate);
            unset($this->target);
        }

        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        //権限
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->cmd = VARS::request("cmd");

        //左フレームから送られてくる
        //年度
        if (VARS::get("EXP_YEAR")) {
            $this->exp_year = VARS::get("EXP_YEAR");
        }
        //学期
        if (VARS::get("EXP_SEMESTER")) {
            $this->exp_semester = VARS::get("EXP_SEMESTER");
        }
        //学籍番号
        if (VARS::get("SCHREGNO")) {
            $this->schregno = VARS::get("SCHREGNO");
        }
        //氏名
        if (VARS::get("NAME")) {
            $this->name = VARS::get("NAME");
        }
        //年組
        if (VARS::get("GRADE")) {
            $pos = strpos(VARS::get("GRADE"), "-");//ハイフンを含むときは、HR_CLASSはセットしない
            if ($pos === false) {
                $this->grade = VARS::get("GRADE")."-".VARS::get("HR_CLASS");
            } else {
                $this->grade = VARS::get("GRADE");
            }
        }
        //更新日(別画面に引き渡す値。コンボで選択した日付)
        if (VARS::post("UPDDATE")) {
            $this->upddate = VARS::post("UPDDATE");
        }
        //新規を選択した際の日付(入力 or ポップアップで指定した日付)
        if (VARS::post("SELNEWDATE")) {
            $this->selnewdate = VARS::post("SELNEWDATE");
        }

        $this->field2 = array("YEAR"            =>  VARS::post("YEAR"),         //更新年度
                              "ENTRANT_NAME"    =>  VARS::post("ENTRANT_NAME"), //記入者
                              "SELECT_COUNT"    =>  VARS::post("SELECT_COUNT"), //項目数
                              "RENKEI_ZYOUHOU"  =>  VARS::post("RENKEI_ZYOUHOU")  //連携の記録
                             );
        $this->target       = VARS::get("TARGET");

        //基礎情報選択の格納
        for ($idx = 1;$idx <= $this->field2["SELECT_COUNT"];$idx++) {
            $this->field2["ZYOUHOU".$idx."_1"] = VARS::post("ZYOUHOU".$idx."_1");
            $this->field2["ZYOUHOU".$idx."_2"] = VARS::post("ZYOUHOU".$idx."_2");
            $this->field2["ZYOUHOU".$idx."_3"] = VARS::post("ZYOUHOU".$idx."_3");
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //各項目のサイズ設定
        $this->getFieldSize();

    }

    //各項目のサイズ設定
    function getFieldSize() {
        $this->kiso_zyouhou_moji = array();
        $this->kiso_zyouhou_gyou = array();
        //基礎情報の文字数と行数の設定
        $this->kiso_zyouhou_moji[1] = 15;
        $this->kiso_zyouhou_gyou[1] = 20;
        $this->kiso_zyouhou_moji[2] = 12;
        $this->kiso_zyouhou_gyou[2] = 20;
        $this->kiso_zyouhou_moji[3] = 12;
        $this->kiso_zyouhou_gyou[3] = 20;
        $this->renkei_zyouhou_moji = 45;
        $this->renkei_zyouhou_gyou = 10;
    }
    
    //更新用のモデルを返します
    function &getUpdateModel()
    {
        $db = Query::dbCheckOut();

        //更新処理
        if ($this->cmd === 'subform1_update') {
            if ($this->validation()) {
                //SCHREG_CHALLENGED_SUPPORTPLAN_STATUS_DATの更新
                knje460n_kiso_zyouhouQuery::getUpdateStatusDat($db, $this);
                //SCHREG_CHALLENGED_SUPPORTPLAN_MAIN_DATの更新
                knje460n_kiso_zyouhouQuery::getUpdateMainDat($db, $this);

                //2.支援をする上での基礎情報
                knje460n_kiso_zyouhouQuery::getUpdateMainQuery($db, $this, '02', '01', $this->field2["RENKEI_ZYOUHOU"]);
                $this->setMessage("MSG201");
            }
        }

        Query::dbCheckIn($db);

        return;
    }
    function validation() {
        //支援内容/支援機関・支援者/支援内容
        for ($cntidx = 1;$cntidx <= 6;$cntidx++) {
            for ($subidx = 1;$subidx <= 3;$subidx++) {
                $idxStr = "ZYOUHOU".$cntidx."_".$subidx;
                if ($this->field2[$idxStr] != "") {
                    if ($this->validate_row_cnt($this->field2[$idxStr], ($this->kiso_zyouhou_moji[$subidx] * 2)) > $this->kiso_zyouhou_gyou[$subidx]) {
                        $sStr1 = "";
                        if ($subidx == 2) {
                            $sStr1 = "支援機関・支援者";
                        } else if ($subidx == 3) {
                            $sStr1 = "支援内容に関する評価";
                        } else {
                            $sStr1 = "支援内容";
                        }
                        $sStr2 = "";
                        if ($cntidx == "2") {
                            $sStr2 = "家庭生活";
                        } else if ($cntidx == "3") {
                            $sStr2 = "余暇・地域生活・福祉";
                        } else if ($cntidx == "4") {
                            $sStr2 = "進路・労働";
                        } else if ($cntidx == "5") {
                            $sStr2 = "医療 ";
                        } else if ($cntidx == "6") {
                            $sStr2 = "その他(カウンセリング等)";
                        } else {
                            $sStr2 = "学校生活";
                        }
                        $this->setWarning("MSG915", $sStr2." ".$sStr1);
                        return false;
                    }
                }
            }
        }

        //連携の記録
        $checkLen = ($this->renkei_zyouhou_moji * 3 + 2) * ($this->renkei_zyouhou_gyou - 1) + ($this->renkei_zyouhou_moji * 3);
        if ($this->field2["RENKEI_ZYOUHOU"] != "") {
            if (strlen($this->field2["RENKEI_ZYOUHOU"]) > $checkLen) {
                $this->setWarning("MSG915", "連携の記録");
                return false;
            }
        }
        
        return true;
    }


}
?>

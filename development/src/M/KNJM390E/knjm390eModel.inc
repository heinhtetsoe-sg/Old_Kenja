<?php

require_once('for_php7.php');

class knjm390eModel extends Model {
    var $field  = array();      //画面データ格納
    var $setdata = array();     //表示データ配列
    var $delback = array();     //削除データ配列
    var $schnodel;              //削除データ
    var $schcntall;             //登録データ数
    var $cmd;                   //コマンド
    var $sch;                   //更新用
    var $schnm;                 //更新用
    var $chir;                  //更新用
    var $date;                  //更新用
    var $peri;                  //更新用
    var $stf ;                  //更新用
    var $crs ;                  //更新用
    var $rmk ;                  //更新用
    var $time ;                 //更新用
    var $schregno;              //サブフォーム用
    var $remark;                //サブフォーム用
    var $Year;                  //年度
    var $semester;              //学期
    var $semcheck;              //データ更新時に使用
    var $Date;                  //日付
    var $User;                  //0:管理者 1:職員
    var $control;               //コントロールマスタ

    function knjm390eModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd = VARS::request("cmd");
        $this->Year = CTRL_YEAR;

        if ($this->cmd != 'update' && $this->cmd != 'addread' && $this->cmd != 'read' && $this->cmd != 'reset') {
            $this->Date  = VARS::post("DATE");
            if ($this->Date == "") $this->Date  = str_replace("-","/",CTRL_DATE);
            $this->field = array("COURSE"           =>  VARS::post("COURSE"),       //スクーリング種別
                                 "CHAIR"            =>  VARS::post("CHAIR"),        //講座
                                 "PERIOD"           =>  VARS::post("PERIOD"),       //校時
                                 "STAFF"            =>  VARS::post("STAFF"),        //担当者
                                 "CREDIT_TIME"      =>  VARS::post("CREDIT_TIME"),  //単位時間
                                 "REMARK"           =>  VARS::post("REMARK"),       //備考
                                 "SCHREGNO"         =>  VARS::post("SCHREGNO"),     //学籍番号
                                 "NAME_KANA"        =>  VARS::post("NAME_KANA"),    //氏名（かな）
                                 "GRADE"            =>  VARS::post("GRADE"),        //学年
                                 "sonotaNotChk"     =>  VARS::post("sonotaNotChk"), //1ならその他をチェックしない
                                 "sonotaConfFlg"    =>  VARS::post("sonotaConfFlg") //1なら確認メッセージを表示する  
                                );
        }
        $this->semester = 0;                //データ更新時に使用
        if ($this->control["学期数"] == 3) {
            if ($this->control["学期開始日付"]["1"] <= $this->Date && $this->control["学期終了日付"]["1"] >= $this->Date) $this->semester = 1;
            if ($this->control["学期開始日付"]["2"] <= $this->Date && $this->control["学期終了日付"]["2"] >= $this->Date) $this->semester = 2;
            if ($this->control["学期開始日付"]["3"] <= $this->Date && $this->control["学期終了日付"]["3"] >= $this->Date) $this->semester = 3;              //データ更新時に使用
        } else {
            if ($this->control["学期開始日付"]["1"] <= $this->Date && $this->control["学期終了日付"]["1"] >= $this->Date) $this->semester = 1;
            if ($this->control["学期開始日付"]["2"] <= $this->Date && $this->control["学期終了日付"]["2"] >= $this->Date) $this->semester = 2;
        }
        //年度内チェック：エラー時は、年度と学期を今年度・今学期にする。
        if ($this->semester == 0) {
            $this->Date  = str_replace("-","/",CTRL_DATE);
            $this->semester = CTRL_SEMESTER;
            $this->setWarning("MSG915","(年度範囲外)");
            return false;
        }
        if ($this->Date > str_replace("-","/",CTRL_DATE)) {
            $this->Date  = str_replace("-","/",CTRL_DATE);
            $this->semester = CTRL_SEMESTER;
            $this->setWarning("MSG915","(処理日付がオーバーしています)");
            return false;
        }

        if ($this->cmd != "update" && $this->cmd != "add" && $this->cmd != "chdel") {
            $this->setdata = array();
        } else {
            for ($ini = 0 ; $ini < $this->schcntall ; $ini++) {
                $this->setdata["DELCHK"][$ini]    = VARS::post("DELCHK".$ini);
                $this->setdata["SCHREGNO2"][$ini] = VARS::post("SCHREGNO2".$ini);
                $this->setdata["NAME"][$ini]      = VARS::post("NAME".$ini);
                $this->setdata["C_TIME"][$ini]    = VARS::post("C_TIME".$ini);
                $this->setdata["T_TIME"][$ini]    = VARS::post("T_TIME".$ini);
                $this->setdata["REMARK2"][$ini]   = VARS::post("REMARK2".$ini);
            }
        }

        if ($this->field["GRADE"])
            $this->field["GRADE"] = sprintf("%02d",$this->field["GRADE"]);

        $this->getPropertiesAll();  //プロパティファイルの読込み
    }

    //入力項目チェック
    function validateInput() {
        $db = Query::dbCheckOut();
        if (VARS::request("cmd") == 'add') {
            if (!$this->field["SCHREGNO"]) {
                $this->setWarning("学籍番号を入力して下さい。");
                return false;
            }            
            if ($this->field["COURSE"] == 1 || $this->field["COURSE"] == 3 || $this->field["COURSE"] == 4) {
                $query = knjm390eQuery::getCheckDatePeriod($this, 1);
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if ($row) {
                    $this->setWarning("既にこの時間は登録されています。登録できません。");
                    return false;
                }
            }
            
            //updateFlgを設定
            $this->updateFlg = 'true';
            //チェック用データ取得
            //開講しない科目、講座情報取得
            $not_chairdat_checkFlg = false;
            if ($this->not_chairdat > 0) {
                $query = knjm390eQuery::getAttendChairData($this, "1");
                $result = $db->query($query);
                $rowcheck = $db->getRow($query, DB_FETCHMODE_ASSOC);
                while ($rowcheck = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $not_chairdat_checkFlg = true;
                    //チェック学籍番号取得
                    $query = knjm390eQuery::getCheckout($this, $rowcheck);
                    $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                    if (!$row) {
                        $this->setWarning("講座に登録されていない学籍番号です");
                        return false;
                    }
                    if ($this->field["COURSE"] == 1 || $this->field["COURSE"] == 4) {
                        if ($this->field["COURSE"] == 1 && $this->Properties["knjm390NoCheckSchedule"] != '1') {
                            if ($this->field["CREDIT_TIME"] > 1) {
                                for ($i=$this->field["PERIOD"]; $i < $this->field["CREDIT_TIME"]+$this->field["PERIOD"]; $i++) {
                                    $query = knjm390eQuery::getCheckSchChrT($this, $rowcheck, $i);
                                    if ($db->getOne($query) == 0) {
                                        $jikFlg = true;
                                    }
                                    $jikanwari = ($jikFlg) ? 0: 1;
                                }
                            } else {
                                $query = knjm390eQuery::getCheckSchChrT($this, $rowcheck, $this->field["PERIOD"]);
                                $jikanwari = $db->getOne($query);
                            }
                            if ($jikanwari == 0) {
                                $this->setWarning("時間割作成がされていません。");
                                return false;
                            }
                        }
                    } else if ($this->field["COURSE"] == 3) {
                        if ($this->field["sonotaNotChk"] == '1') {
                            $this->field["sonotaConfFlg"] = '';
                            $this->field["sonotaNotChk"] = '';
                        } else {
                            $query = knjm390eQuery::getCheckDatePeriod($this, 2, $rowcheck);
                            $hiduke = $db->getOne($query);
                            if ($hiduke) {
                                $this->field["sonotaConfFlg"] = '1';
                                $this->updateFlg = 'false';
                            }
                        }
                    }
                    if (strlen($this->field["REMARK"]) > 60) {
                        $this->setWarning("MSG915",strlen($this->field["REMARK"])."(備考は60バイトまでです)");
                        return false;
                    }
                    if (VARS::post("COURSE") == 1 || VARS::post("COURSE") == 4) {
                        $query = knjm390eQuery::getCheckIns($this, $rowcheck);
                        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                        if ($row){
                            $this->setWarning("既に登録されています。");
                            return false;
                        }
                    }
                }
            }
            //コンボ選択講座のチェック学籍番号取得
            $query = knjm390eQuery::getCheckout($this, "");
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (!$row) {
                //開講しない科目、講座情報取得している場合は
                if ($this->not_chairdat == 0 || $not_chairdat_checkFlg == false) {
                    $this->setWarning("講座に登録されていない学籍番号です");
                    return false;
                } else {
                    $this->updateFlg = 'false';
                }
            }
            if ($this->field["COURSE"] == 1 || $this->field["COURSE"] == 4) {
                if ($this->field["COURSE"] == 1 && $this->Properties["knjm390NoCheckSchedule"] != '1') {
                    if ($this->field["CREDIT_TIME"] > 1) {
                        $jikFlg = false;
                        for ($i=$this->field["PERIOD"]; $i < $this->field["CREDIT_TIME"]+$this->field["PERIOD"]; $i++) {
                            $query = knjm390eQuery::getCheckSchChrT($this, $rowcheck, $i);
                            if ($db->getOne($query) == 0) {
                                $jikFlg = true;
                            }
                            $jikanwari = ($jikFlg) ? 0: 1;
                        }
                    } else {
                        $query = knjm390eQuery::getCheckSchChrT($this, "", $this->field["PERIOD"]);
                        $jikanwari = $db->getOne($query);
                    }
                    if ($jikanwari == 0) {
                        if ($this->not_chairdat == 0 || $not_chairdat_checkFlg == false) {
                            $this->setWarning("時間割作成がされていません。");
                            return false;
                        } else {
                            $this->updateFlg = 'false';
                        }
                    }
                }
            } else if ($this->field["COURSE"] == 3) {
                if ($this->field["sonotaNotChk"] == '1') {
                    $this->field["sonotaConfFlg"] = '';
                    $this->field["sonotaNotChk"] = '';
                } else {
                    $query = knjm390eQuery::getCheckDatePeriod($this, 2, $rowcheck);
                    $hiduke = $db->getOne($query);
                    if ($hiduke) {
                        $this->field["sonotaConfFlg"] = '1';
                        $this->updateFlg = 'false';
                    }
                }
            }
            if (strlen($this->field["REMARK"]) > 60) {
                $this->setWarning("MSG915",strlen($this->field["REMARK"])."(備考は60バイトまでです)");
                return false;
            }
            if (VARS::post("COURSE") == 1 || VARS::post("COURSE") == 4) {
                $query = knjm390eQuery::getCheckIns($this, "");
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if ($row){
                    if ($this->not_chairdat == 0 || $not_chairdat_checkFlg == false) {
                        $this->setWarning("既に登録されています。");
                        return false;
                    } else {
                        $this->updateFlg = 'false';
                    }
                }
            }
        }

        if (VARS::request("cmd") == 'update') {
            if (strlen(VARS::post("REMARKSUB")) > 60) {
                $this->setWarning("MSG915",strlen(VARS::post("REMARKSUB"))."(備考は60バイトまでです)");
                return false;
            }
        }

        Query::dbCheckIn($db);
        return true;
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()){
            if (VARS::request("cmd") == 'add') {
                $this->insert();
            } else if (VARS::request("cmd") == 'update') {
                $this->update();
            } else {
                $this->delback = array();
                $delc = 0;
                for ($i = 0;$i < $this->schcntall;$i++) {
                    if ($this->setdata["DELCHK"][$i]=="on") {
                        $this->delback[$delc] = $this->setdata["SCHREGNO2"][$i].','.$this->setdata["T_TIME"][$i];
                        $delc++;
                    }
                }
                $this->schnodel = array();
                for ($i = 0;$i < get_count($this->delback);$i++) {
                    $this->schnodel[] = $this->delback[$i];
                }
                if (get_count($this->delback) == 0) {
                    $this->setWarning("削除データを選択して下さい。");
                    return false;
                }
                $this->deletesitei();
            }
        }
    }

    //インサート時の内部メソッド
    function &insert() {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        //開講しない科目の講座の出席追加
        if ($this->not_chairdat > 0) {
            $query = knjm390eQuery::getAttendChairData($this, "1");
            $result = $db->query($query);
            while ($rowcheck = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if (VARS::post("COURSE") == 3 && $this->field["sonotaConfFlg"]) {
                } else {
                    // 単位時間が2時間以上の時、データを指定時間個数（例：2時間 → 2レコード）作る
                    if ($this->field["CREDIT_TIME"] > 1) {
                        for ($i=$this->field["PERIOD"]; $i < $this->field["CREDIT_TIME"]+$this->field["PERIOD"]; $i++) {
                            $query = knjm390eQuery::getDeleteSchAttendDat($this, $rowcheck, $i);
                            $db->query($query);
                            $query = knjm390eQuery::getInsertQuery($this, $rowcheck, $i);
                            $db->query($query);
                        }
                    } else {
                        $query = knjm390eQuery::getInsertQuery($this, $rowcheck, $this->field["PERIOD"]);
                        $db->query($query);
                    }
                }
                if (VARS::post("COURSE") == 1 || VARS::post("COURSE") == 4) {
                    if ($this->field["CREDIT_TIME"] > 1) {
                        for ($i=$this->field["PERIOD"]; $i < $this->field["CREDIT_TIME"]+$this->field["PERIOD"]; $i++) {
                            $query = knjm390eQuery::getUpdateSchchr($this, $rowcheck, $i);
                            $db->query($query);
                        }
                    } else {
                        $query = knjm390eQuery::getUpdateSchchr($this, $rowcheck, $this->field["PERIOD"]);
                        $db->query($query);
                    }
                }
            }
        }
        //コンボ選択講座の出席追加
        if ($this->updateFlg === 'true') {
            // 単位時間が2時間以上の時、データを指定時間個数（例：2時間 → 2レコード）作る
            if ($this->field["CREDIT_TIME"] > 1) {
                for ($i=$this->field["PERIOD"]; $i < $this->field["CREDIT_TIME"]+$this->field["PERIOD"]; $i++) {
                    $query = knjm390eQuery::getDeleteSchAttendDat($this, "", $i);
                    $db->query($query);
                    $query = knjm390eQuery::getInsertQuery($this, "", $i);
                    $db->query($query);
                }
            } else {
                $query = knjm390eQuery::getInsertQuery($this, "", $this->field["PERIOD"]);
                $db->query($query);
            }
            if (VARS::post("COURSE") == 1 || VARS::post("COURSE") == 4) {
                if ($this->field["CREDIT_TIME"] > 1) {
                    for ($i=$this->field["PERIOD"]; $i < $this->field["CREDIT_TIME"]+$this->field["PERIOD"]; $i++) {
                        $query = knjm390eQuery::getUpdateSchchr($this, "", $i);
                        $db->query($query);
                    }
                } else {
                    $query = knjm390eQuery::getUpdateSchchr($this, "", $this->field["PERIOD"]);
                    $db->query($query);
                }
            }
        }
        $db->commit();      // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }

    //アップデート時の内部メソッド
    function &update() {
        knjm390eQuery::getUpdateQuery($this);
    }

    //デリート時の内部メソッド
    function &deletesitei() {
        knjm390eQuery::getDeleteQuerysitei($this);
    }
}
?>

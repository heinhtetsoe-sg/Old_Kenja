<?php

require_once('for_php7.php');

class knjm392eModel extends Model {
    var $field  = array();      //画面データ格納
    var $setdata = array();     //表示データ配列
    var $schcntall;             //登録データ数
    var $insdata = array();     //追加データ配列
    var $upddata = array();     //更新データ配列
    var $deldata = array();     //削除データ配列
    var $cmd;                   //コマンド
    var $Year;                  //年度
    var $semester;              //学期
    var $semcheck;              //データ更新時に使用
    var $Date;                  //日付
    var $User;                  //0:管理者 1:職員
    var $control;               //コントロールマスタ

    function knjm392eModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd = VARS::request("cmd");
        $this->Year = CTRL_YEAR;

        if ($this->cmd != 'addread' && $this->cmd != 'read' && $this->cmd != 'sort') {
            $this->Date  = ($this->cmd == 'add') ? VARS::post("DIS_DATE") : VARS::post("DATE");
            if ($this->Date == "") $this->Date  = str_replace("-","/",CTRL_DATE);
            $this->field = array("COURSE"           =>  ($this->cmd == 'add') ? VARS::post("DIS_COURSE") : VARS::post("COURSE"),       //スクーリング種別
                                 "CHAIR"            =>  ($this->cmd == 'add') ? VARS::post("DIS_CHAIR")  : VARS::post("CHAIR"),        //講座
                                 "PERIOD"           =>  ($this->cmd == 'add') ? VARS::post("DIS_PERIOD") : VARS::post("PERIOD"),       //校時
                                 "STAFF"            =>  ($this->cmd == 'add') ? VARS::post("DIS_STAFF")  : VARS::post("STAFF"),        //担当者
                                 "CREDIT_TIME"      =>  VARS::post("CREDIT_TIME"),  //単位時間
                                 "REMARK"           =>  VARS::post("REMARK"),       //備考
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

        if ($this->cmd != "add" && $this->cmd != "chdel") {
            $this->setdata = array();
        } else {
            foreach ($this->schregNos as $schregNo) {
                $this->setdata["SCHREGNO"][$schregNo]   = VARS::post("SCHREGNO".$schregNo);
                $this->setdata["CHECK"][$schregNo]      = VARS::post("CHECK-".$schregNo);
                $this->setdata["T_TIME"][$schregNo]     = VARS::post("T_TIME".$schregNo);
                $this->setdata["AT_CHAIRCD"][$schregNo] = VARS::post("AT_CHAIRCD".$schregNo);
                //備考
                $this->setdata["REMARK"][$schregNo]     = VARS::post("REMARK".$schregNo);
                $this->setdata["PRE_REMARK"][$schregNo] = VARS::post("PRE_REMARK".$schregNo);
                //チェックした生徒のみ処理する。
                if ($this->setdata["CHECK"][$schregNo] == '1') {
                    $this->setdata["insFlg"][$schregNo] = "ok";
                } else {
                    $this->setdata["insFlg"][$schregNo] = "skip";
                }
            }
        }

        if (VARS::get("PROGRAMID")) {
            unset($this->getSort);
            //ソート処理の初期化
            $this->sort["SRT_SCHREGNO"] = 1;
            $this->sort["SRT_HR_NAME"]  = 1;
        }

        if (VARS::get("sort")) {
            $this->getSort = VARS::get("sort");
            $this->sort[$this->getSort] = $this->sort[$this->getSort] == 1 ? 2 : 1;
        }

        $this->getPropertiesAll();  //プロパティファイルの読込み
    }

    //入力項目チェック
    function validateInput() {
        $db = Query::dbCheckOut();
        //ins
        for ($i = 0; $i < get_count($this->insdata); $i++) {
            list($schregno, $receipt_time, $remark) = preg_split("/,/", $this->insdata[$i]);

            if (!$schregno) {
                $this->setWarning("学籍番号を入力して下さい。");
                return false;
            }
            if ($this->field["COURSE"] == 1 || $this->field["COURSE"] == 3 || $this->field["COURSE"] == 4) {
                $query = knjm392eQuery::getCheckDatePeriod($this, 1, "", $schregno);
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
                $query = knjm392eQuery::getAttendChairData($this, "1", $schregno);
                $result = $db->query($query);
                $rowcheck = $db->getRow($query, DB_FETCHMODE_ASSOC);
                while ($rowcheck = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $not_chairdat_checkFlg = true;
                    //チェック学籍番号取得
                    $query = knjm392eQuery::getCheckout($this, $rowcheck, $schregno);
                    $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                    if (!$row) {
                        $this->setWarning("講座に登録されていない学籍番号です");
                        return false;
                    }
                    if ($this->field["COURSE"] == 1 || $this->field["COURSE"] == 4) {
                        if ($this->field["COURSE"] == 1 && $this->Properties["knjm390NoCheckSchedule"] != '1') {
                            if ($this->field["CREDIT_TIME"] > 1) {
                                for ($j=$this->field["PERIOD"]; $j < $this->field["CREDIT_TIME"]+$this->field["PERIOD"]; $j++) {
                                    $query = knjm392eQuery::getCheckSchChrT($this, $rowcheck, $j);
                                    if ($db->getOne($query) == 0) {
                                        $jikFlg = true;
                                    }
                                    $jikanwari = ($jikFlg) ? 0: 1;
                                }
                            } else {
                                $query = knjm392eQuery::getCheckSchChrT($this, $rowcheck, $this->field["PERIOD"]);
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
                            $query = knjm392eQuery::getCheckDatePeriod($this, 2, $rowcheck, $schregno);
                            $hiduke = $db->getOne($query);
                            if ($hiduke) {
                                $this->field["sonotaConfFlg"] = '1';
                                $this->updateFlg = 'false';
                            }
                        }
                    }
                    if ($this->field["COURSE"] == 1 || $this->field["COURSE"] == 4) {
                        $query = knjm392eQuery::getCheckIns($this, $rowcheck, $schregno);
                        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                        if ($row){
                            $this->setWarning("既に登録されています。");
                            return false;
                        }
                    }
                }
            }
            //コンボ選択講座のチェック学籍番号取得
            $query = knjm392eQuery::getCheckout($this, "", $schregno);
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
                        for ($j=$this->field["PERIOD"]; $j < $this->field["CREDIT_TIME"]+$this->field["PERIOD"]; $j++) {
                            $query = knjm392eQuery::getCheckSchChrT($this, $rowcheck, $j);
                            if ($db->getOne($query) == 0) {
                                $jikFlg = true;
                            }
                            $jikanwari = ($jikFlg) ? 0: 1;
                        }
                    } else {
                        $query = knjm392eQuery::getCheckSchChrT($this, "", $this->field["PERIOD"]);
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
                    $query = knjm392eQuery::getCheckDatePeriod($this, 2, $rowcheck, $schregno);
                    $hiduke = $db->getOne($query);
                    if ($hiduke) {
                        $this->field["sonotaConfFlg"] = '1';
                        $this->updateFlg = 'false';
                    }
                }
            }
            if ($this->field["COURSE"] == 1 || $this->field["COURSE"] == 4) {
                $query = knjm392eQuery::getCheckIns($this, "", $schregno);
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
        }//for
        Query::dbCheckIn($db);
        return true;
    }

    // 削除用のモデルを返します
    function &getDeleteModel() {
        $this->deldata = array();
        foreach ($this->schregNos as $schregNo) {
            if ($this->setdata["insFlg"][$schregNo] == "skip") continue;
            $this->deldata[] = $this->setdata["SCHREGNO"][$schregNo].','.$this->setdata["T_TIME"][$schregNo].','.$this->setdata["REMARK"][$schregNo].','.$this->setdata["AT_CHAIRCD"][$schregNo];
        }
        knjm392eQuery::getDeleteQuerysitei($this);
        $this->setMessage("MSG201");
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        //◆データ変更した生徒のみ処理する。
        //  初期表示した時、データを保持し、入力値と比較し、判断する。
        $this->insdata = array();
        foreach ($this->schregNos as $schregNo) {
            if ($this->setdata["insFlg"][$schregNo] == "skip") continue;
            if (strlen($this->setdata["REMARK"][$schregNo]) > 60) {
                $this->setWarning("MSG915", strlen($this->setdata["REMARK"][$schregNo])."(備考は60バイトまでです)");
                return false;
            }
            $this->insdata[] = $this->setdata["SCHREGNO"][$schregNo].','.$this->setdata["T_TIME"][$schregNo].','.$this->setdata["REMARK"][$schregNo];
        }
        //ins
        if (0 < get_count($this->insdata)) {
            if ($this->validateInput()){
                $this->insert();
            }
        }
        /*** デバック用
        $insKensuu = get_count($this->insdata);
        $this->setMessage("MSG201","処理件数（追加：{$insKensuu}件）です。");
        ***/
        if (0 < get_count($this->insdata)) {
            $this->setMessage("MSG201");
        } else {
            $this->setMessage("MSG202");
        }
    }

    //インサート時の内部メソッド
    function &insert() {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        //ins
        for ($i = 0; $i < get_count($this->insdata); $i++) {
            list($schregno, $receipt_time, $remark) = preg_split("/,/", $this->insdata[$i]);
            //開講しない科目の講座の出席追加
            if ($this->not_chairdat > 0) {
                $query = knjm392eQuery::getAttendChairData($this, "1", $schregno);
                $result = $db->query($query);
                while ($rowcheck = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($this->field["COURSE"] == 3 && $this->field["sonotaConfFlg"]) {
                    } else {
                        // 単位時間が2時間以上の時、データを指定時間個数（例：2時間 → 2レコード）作る
                        if ($this->field["CREDIT_TIME"] > 1) {
                            for ($j=$this->field["PERIOD"]; $j < $this->field["CREDIT_TIME"]+$this->field["PERIOD"]; $j++) {
                                $query = knjm392eQuery::getDeleteSchAttendDat($this, $rowcheck, $schregno, $j);
                                $db->query($query);
                                $query = knjm392eQuery::getInsertQuery($this, $rowcheck, $schregno, $j, $remark);
                                $db->query($query);
                            }
                        } else {
                            $query = knjm392eQuery::getInsertQuery($this, $rowcheck, $schregno, $this->field["PERIOD"], $remark);
                            $db->query($query);
                        }
                    }
                    if ($this->field["COURSE"] == 1 || $this->field["COURSE"] == 4) {
                        if ($this->field["CREDIT_TIME"] > 1) {
                            for ($j=$this->field["PERIOD"]; $j < $this->field["CREDIT_TIME"]+$this->field["PERIOD"]; $j++) {
                                $query = knjm392eQuery::getUpdateSchchr($this, $rowcheck, $j);
                                $db->query($query);
                            }
                        } else {
                            $query = knjm392eQuery::getUpdateSchchr($this, $rowcheck, $this->field["PERIOD"]);
                            $db->query($query);
                        }
                    }
                }
            }
            //コンボ選択講座の出席追加
            if ($this->updateFlg === 'true') {
                // 単位時間が2時間以上の時、データを指定時間個数（例：2時間 → 2レコード）作る
                if ($this->field["CREDIT_TIME"] > 1) {
                    for ($j=$this->field["PERIOD"]; $j < $this->field["CREDIT_TIME"]+$this->field["PERIOD"]; $j++) {
                        $query = knjm392eQuery::getDeleteSchAttendDat($this, "", $schregno, $j);
                        $db->query($query);
                        $query = knjm392eQuery::getInsertQuery($this, "", $schregno, $j, $remark);
                        $db->query($query);
                    }
                } else {
                    $query = knjm392eQuery::getInsertQuery($this, "", $schregno, $this->field["PERIOD"], $remark);
                    $db->query($query);
                }
                if ($this->field["COURSE"] == 1 || $this->field["COURSE"] == 4) {
                    if ($this->field["CREDIT_TIME"] > 1) {
                        for ($j=$this->field["PERIOD"]; $j < $this->field["CREDIT_TIME"]+$this->field["PERIOD"]; $j++) {
                            $query = knjm392eQuery::getUpdateSchchr($this, "", $j);
                            $db->query($query);
                        }
                    } else {
                        $query = knjm392eQuery::getUpdateSchchr($this, "", $this->field["PERIOD"]);
                        $db->query($query);
                    }
                }
            }
        }//for
        $db->commit();      // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }
}
?>

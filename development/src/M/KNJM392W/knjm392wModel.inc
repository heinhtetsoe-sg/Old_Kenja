<?php

require_once('for_php7.php');

class knjm392wModel extends Model {
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

    function knjm392wModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd = VARS::request("cmd");
        $this->Year = CTRL_YEAR;

        if ($this->cmd != 'update' && $this->cmd != 'addread' && $this->cmd != 'read' && $this->cmd != 'reset') {
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

        if ($this->cmd != "update" && $this->cmd != "add" && $this->cmd != "alldel" && $this->cmd != "chdel") {
            $this->setdata = array();
        } else {
            for ($ini = 0 ; $ini < $this->schcntall ; $ini++) {
                $this->setdata["SCHREGNO"][$ini] = VARS::post("SCHREGNO".$ini);
                $this->setdata["T_TIME"][$ini] = VARS::post("T_TIME".$ini);
                $this->setdata["AT_CHAIRCD"][$ini] = VARS::post("AT_CHAIRCD".$ini);
                //出席者、備考
                $this->setdata["ATTEND"][$ini] = VARS::post("ATTEND".$ini);
                $this->setdata["REMARK"][$ini] = VARS::post("REMARK".$ini);
                $this->setdata["PRE_ATTEND"][$ini] = VARS::post("PRE_ATTEND".$ini);
                $this->setdata["PRE_REMARK"][$ini] = VARS::post("PRE_REMARK".$ini);
                //◆データ変更した生徒のみ処理する。
                if ($this->setdata["PRE_ATTEND"][$ini] != $this->setdata["ATTEND"][$ini] ||
                    $this->setdata["PRE_REMARK"][$ini] != $this->setdata["REMARK"][$ini]) {
                    $this->setdata["insFlg"][$ini] = "ok";
                } else {
                    $this->setdata["insFlg"][$ini] = "skip";
                }
                if (!strlen($this->setdata["ATTEND"][$ini]) && strlen($this->setdata["REMARK"][$ini])) {
                    $this->setdata["cmd"][$ini] = "err";
                } else if (!strlen($this->setdata["ATTEND"][$ini]) && !strlen($this->setdata["REMARK"][$ini])) {
                    $this->setdata["cmd"][$ini] = "del";
                } else if ( strlen($this->setdata["ATTEND"][$ini]) && !strlen($this->setdata["REMARK"][$ini])) {
                    $this->setdata["cmd"][$ini] = strlen($this->setdata["PRE_ATTEND"][$ini]) ? "upd" : "ins";
                } else if ( strlen($this->setdata["ATTEND"][$ini]) &&  strlen($this->setdata["REMARK"][$ini])) {
                    $this->setdata["cmd"][$ini] = strlen($this->setdata["PRE_ATTEND"][$ini]) ? "upd" : "ins";
                }
            }
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
            if ($this->Properties["schAttendDatNoCheckDatePeriod"] != '1') {
                if ($this->field["COURSE"] == 1 || $this->field["COURSE"] == 3 || $this->field["COURSE"] == 4) {
                    $query = knjm392wQuery::getCheckDatePeriod($this, 1, "", $schregno);
                    $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                    if ($row) {
                        $this->setWarning("既にこの時間は登録されています。登録できません。");
                        return false;
                    }
                }
            }

            //updateFlgを設定
            $this->updateFlg = 'true';
            //チェック用データ取得
            //開講しない科目、講座情報取得
            $not_chairdat_checkFlg = false;
            if ($this->not_chairdat > 0) {
                $query = knjm392wQuery::getAttendChairData($this, "1", $schregno);
                $result = $db->query($query);
                $rowcheck = $db->getRow($query, DB_FETCHMODE_ASSOC);
                while ($rowcheck = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $not_chairdat_checkFlg = true;
                    //チェック学籍番号取得
                    $query = knjm392wQuery::getCheckout($this, $rowcheck, $schregno);
                    $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                    if (!$row) {
                        $this->setWarning("講座に登録されていない学籍番号です");
                        return false;
                    }
                    if ($this->field["COURSE"] == 1 || $this->field["COURSE"] == 4) {
                        $query = knjm392wQuery::getCheckDatePeriod($this, 2, $rowcheck, $schregno);
                        $hiduke = $db->getOne($query);
                        if ($hiduke) {
                            $this->setWarning("同日2回目の登録です。登録できません。");
                            return false;
                        }
                        if ($this->field["COURSE"] == 1) {
                            $query = knjm392wQuery::getCheckSchChrT($this, $rowcheck);
                            $jikanwari = $db->getOne($query);
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
                            $query = knjm392wQuery::getCheckDatePeriod($this, 2, $rowcheck, $schregno);
                            $hiduke = $db->getOne($query);
                            if ($hiduke) {
                                $this->field["sonotaConfFlg"] = '1';
                                $this->updateFlg = 'false';
                            }
                        }
                    }
                    if ($this->field["COURSE"] == 1 || $this->field["COURSE"] == 4) {
                        $query = knjm392wQuery::getCheckIns($this, $rowcheck, $schregno);
                        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                        if ($row){
                            $this->setWarning("既に登録されています。");
                            return false;
                        }
                    }
                }
            }
            //コンボ選択講座のチェック学籍番号取得
            $query = knjm392wQuery::getCheckout($this, "", $schregno);
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
                $query = knjm392wQuery::getCheckDatePeriod($this, 2, "", $schregno);
                $hiduke = $db->getOne($query);
                if ($hiduke) {
                    $this->setWarning("同日2回目の登録です。登録できません。");
                    return false;
                }
                if ($this->field["COURSE"] == 1) {
                    $query = knjm392wQuery::getCheckSchChrT($this, "");
                    $jikanwari = $db->getOne($query);
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
                    $query = knjm392wQuery::getCheckDatePeriod($this, 2, $rowcheck, $schregno);
                    $hiduke = $db->getOne($query);
                    if ($hiduke) {
                        $this->field["sonotaConfFlg"] = '1';
                        $this->updateFlg = 'false';
                    }
                }
            }
            if ($this->field["COURSE"] == 1 || $this->field["COURSE"] == 4) {
                $query = knjm392wQuery::getCheckIns($this, "", $schregno);
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

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        //◆データ変更した生徒のみ処理する。
        //  初期表示した時、データを保持し、入力値と比較し、判断する。
        $this->insdata = array();
        $this->upddata = array();
        $this->deldata = array();
        for ($i = 0;$i < $this->schcntall;$i++) {
            if ($this->setdata["insFlg"][$i] == "skip") continue;
            if ($this->setdata["cmd"][$i] == "err") {
                $this->setWarning("出席者がチェックされていません。備考のみ入力はできません。");
                return false;
            }
            if (strlen($this->setdata["REMARK"][$i]) > 60) {
                $this->setWarning("MSG915", strlen($this->setdata["REMARK"][$i])."(備考は60バイトまでです)");
                return false;
            }
            //ins
            if ($this->setdata["cmd"][$i] == "ins") {
                $this->insdata[] = $this->setdata["SCHREGNO"][$i].','.$this->setdata["T_TIME"][$i].','.$this->setdata["REMARK"][$i];
            }
            //upd
            if ($this->setdata["cmd"][$i] == "upd") {
                $this->upddata[] = $this->setdata["SCHREGNO"][$i].','.$this->setdata["T_TIME"][$i].','.$this->setdata["REMARK"][$i].','.$this->setdata["AT_CHAIRCD"][$i];
            }
            //del
            if ($this->setdata["cmd"][$i] == "del") {
                $this->deldata[] = $this->setdata["SCHREGNO"][$i].','.$this->setdata["T_TIME"][$i].','.$this->setdata["REMARK"][$i].','.$this->setdata["AT_CHAIRCD"][$i];
            }
        }
        //ins
        if (0 < get_count($this->insdata)) {
            if ($this->validateInput()){
                $this->insert();
            }
        }
        //upd
        if (0 < get_count($this->upddata)) {
            $this->update();
        }
        //del
        if (0 < get_count($this->deldata)) {
            $this->deletesitei();
        }
        /*** デバック用
        $insKensuu = get_count($this->insdata);
        $updKensuu = get_count($this->upddata);
        $delKensuu = get_count($this->deldata);
        $this->setMessage("MSG201","処理件数（追加：{$insKensuu}件　更新：{$updKensuu}件　削除：{$delKensuu}件）です。");
        ***/
        if (0 < get_count($this->insdata) || 0 < get_count($this->upddata) || 0 < get_count($this->deldata)) {
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
                $query = knjm392wQuery::getAttendChairData($this, "1", $schregno);
                $result = $db->query($query);
                while ($rowcheck = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($this->field["COURSE"] == 3 && $this->field["sonotaConfFlg"]) {
                    } else {
                        $query = knjm392wQuery::getInsertQuery($this, $rowcheck, $schregno, $remark);
                        $db->query($query);
                    }
                    if ($this->field["COURSE"] == 1 || $this->field["COURSE"] == 4) {
                        $query = knjm392wQuery::getUpdateSchchr($this, $rowcheck);
                        $db->query($query);
                    }
                }
            }
            //コンボ選択講座の出席追加
            if ($this->updateFlg === 'true') {
                $query = knjm392wQuery::getInsertQuery($this, "", $schregno, $remark);
                $db->query($query);
                if ($this->field["COURSE"] == 1 || $this->field["COURSE"] == 4) {
                    $query = knjm392wQuery::getUpdateSchchr($this, "");
                    $db->query($query);
                }
            }
        }//for
        $db->commit();      // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }

    //アップデート時の内部メソッド
    function &update() {
        knjm392wQuery::getUpdateQuery($this);
    }

    //デリート時の内部メソッド
    function &delete() {
        knjm392wQuery::getDeleteQuery($this);
    }
    function &deletesitei() {
        knjm392wQuery::getDeleteQuerysitei($this);
    }
}
?>

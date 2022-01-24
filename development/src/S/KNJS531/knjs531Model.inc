<?php

require_once('for_php7.php');

class knjs531Model extends Model {
    var $field = array();
    var $cmd;
    var $staffcd;          //セキュリティー用スタッフコード
    var $sec_competence;   //権限保持
    var $weekArray = array();
    var $updDateArray = array();
    

    function init() {

        if (VARS::get("PROGRAMID") != "") {
            unset($this->batch);
        }

        //共通関数(コントロールマスタより)
        common::GetControlMaster_Fnc($this->control);
        $this->cmd = VARS::request("cmd");
                                     
        $this->field = array("YEAR"                             => VARS::post("YEAR"),
                             "SCHOOL_KIND"                      => VARS::post("SCHOOL_KIND"),
                             "MONTH"                            => VARS::post("MONTH"),
                             "HR_CLASS_DIV"                     => VARS::post("HR_CLASS_DIV"),
                             "GRADE_HR"                         => VARS::post("GRADE_HR"),
                             "SHORI_DIV"                        => VARS::post("SHORI_DIV"),
                             "VISITOR"                          => VARS::post("VISITOR")
                             );

        //日付の初期値をセット
        $day = 1;
        $year = $this->field["MONTH"] > 3 ? $this->field["YEAR"] : $this->field["YEAR"] + 1;
        $last_day = date("t", mktime(0, 0, 0, $this->field["MONTH"], 1, $year));

        //画面切替用
        if ($this->cmd == "batch" || $this->cmd == "back") {
            $this->batch = VARS::post("batch");
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //日付のカウントアップ（対象月の最終日まで）
        $addCnt = 0;
        //曜日のカウントアップ
        $youbiCnt = 0;
        $this->updfield = array();
        for ($i = 0; $i < $last_day; $i++) {
            $setDay = date("Y-m-d", mktime( 0, 0, 0, $this->field["MONTH"], $day + $addCnt, $year));
            if ($this->Properties["useEventAbbv"] == "1") {
                $this->updfield[$setDay]["EVENT_ABBV"] = VARS::post("EVENT_ABBV_".$setDay);
            }
            $this->updfield[$setDay]["REMARK1"] = VARS::post("REMARK1_".$setDay);
            $this->updfield[$setDay]["REMARK2"] = VARS::post("REMARK2_".$setDay);
            $this->updfield[$setDay]["HOLIDAY_FLG"] = VARS::post("HOLIDAY_FLG_".$setDay);
            $addCnt++;
        }
        if ($this->cmd == "") {
            $this->field = array();
        }

    }

    /***************************************
    更新メイン処理
    ****************************************/
    function &getInsertModel() {
        //文字数チェック
        $err = false;
        $errMsg = "";
        foreach ($this->updfield as $executeDate => $val) {
            if (strlen($val["REMARK1"]) > 30) {
                $err = true;
                $errDate = date("Y年m月d日", strtotime($executeDate));
                $errMsg .= "\\n".$errDate;
            }
        }
        if ($err == true) {
            $this->setWarning("MSG915","データ行事名は全角10文字までです。\\n".$errMsg);
            return false;
        }

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //更新処理
        $this->insert($db);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        $this->setMessage("MSG201");

        return;
    }

    /*****************
    更新用の各処理
    ******************/
    //データの更新（日付をkeyにした配列、日付ごとに更新する）
    function insert($db) {
        knjs531Query::getInsertQuery($db, $this);

        if (!($this->Properties["useSpecial_Support_Hrclass"] == '1' && $this->field["SHORI_DIV"] == "2")) {
            //時間割削除
            knjs531Query::delSchChrDat($db, $this, $this->field["GRADE_HR"]);
        }

    }

    //行事予定登録処理
    function &getInsert2Model() {

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $ghr_array = array();
        if ($this->batch) {
            //対象学年の年組一覧取得
            if ($this->field["HR_CLASS_DIV"] == '2') {
                $ghr_array = $db->getCol(knjs531Query::getGradeFiClass($this));
            } else {
                $ghr_array = $db->getCol(knjs531Query::getGradeHrclass($this));
            }
        } else {
            $ghr_array[] = $this->field["GRADE_HR"];
        }

        $msg_flg = false;
        foreach ($ghr_array as $ghr) {
            $messege_flg = false;
            $query = knjs531Query::getEventSchoolcount($this, $ghr, "2");
            $getSchoolevent = $db->getOne($query);
            if ($getSchoolevent > 0) {
                //対象データ削除処理
                $query = knjs531Query::getEventDelQuery($this, $ghr);
                $db->query($query);
                //対象データ取込処理
                $query = knjs531Query::getCopyQuery($this, $ghr, "2");
                $db->query($query);
                $messege_flg = true;
            } else {
                //指定学年のHR_CLASS='000'のデータ
                $query = knjs531Query::getEventSchoolcount($this, $ghr, "2", "all");
                $getGradeSchoolAllevent = $db->getOne($query);
                if ($getGradeSchoolAllevent > 0) {
                    //対象データ削除処理
                    $query = knjs531Query::getEventDelQuery($this, $ghr);
                    $db->query($query);
                    //対象データ取込処理
                    $query = knjs531Query::getCopyQuery($this, $ghr, "2", "all");
                    $db->query($query);
                    $messege_flg = true;
                } else {
                    //学校のマスタデータ
                    $query = knjs531Query::getEventSchoolcount($this, $ghr, "1");
                    $getGradeSchoolevent = $db->getOne($query);
                    if ($getGradeSchoolevent > 0) {
                        //対象データ削除処理
                        $query = knjs531Query::getEventDelQuery($this, $ghr);
                        $db->query($query);
                        //対象データ取込処理
                        $query = knjs531Query::getCopyQuery($this, $ghr, "1");
                        $db->query($query);
                        $messege_flg = true;
                    }
                }
            }

            if ($messege_flg) {
                //時間割削除
                knjs531Query::delSchChrDat($db, $this, $ghr);
            }
            if ($messege_flg == true) $msg_flg = true;
        }

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        if ($msg_flg) {
            $this->setMessage("MSG201");
        } else {
            $this->setMessage("MSG303", "学校、学年の行事マスタを登録して下さい。");
        }

        return;
    }

    //行事のコピー（訪問生）
    function &getInsert3Model() {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $msg_flg = false;

        //行事データ確認
        $query = knjs531Query::getEventcountMonth($this);
        $getEventCnt = $db->getOne($query);

        if ($getEventCnt > 0) {
            //対象データ削除処理
            $query = knjs531Query::getEventSchregDelQuery($this);
            $db->query($query);

            //対象データ取込処理
            $query = knjs531Query::getEventSchregCopyQuery($this);
            $db->query($query);
            $msg_flg = true;
        }

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        if ($msg_flg) {
            $this->setMessage("MSG201");
        } else {
            $this->setMessage("MSG303", "年組の行事データを登録して下さい。");
        }

        return;
    }
}
?>

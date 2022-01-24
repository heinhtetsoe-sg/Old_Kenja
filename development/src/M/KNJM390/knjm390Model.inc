<?php

require_once('for_php7.php');


class knjm390Model extends Model {

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
    var $seq ;                  //更新用
    var $rmk ;                  //更新用
    var $schregno;              //サブフォーム用
    var $remark;                //サブフォーム用
    var $Year;                  //年度
    var $semester;              //学期
    var $semcheck;              //データ更新時に使用
    var $Date;                  //日付
    var $User;                  //0:管理者 1:職員
    var $control;               //コントロールマスタ

    function knjm390Model()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->cmd         = VARS::request("cmd");
        $this->Year = CTRL_YEAR;

        if ($this->cmd != 'update' && $this->cmd != 'addread' && $this->cmd != 'read' && $this->cmd != 'reset'){
            $this->Date  = VARS::post("DATE");
            if ($this->Date == "") $this->Date  = str_replace("-","/",CTRL_DATE);
            $this->field = array("COURSE"           =>  VARS::post("COURSE"),       //スクーリング種別
                                 "CHAIR"            =>  VARS::post("CHAIR"),        //講座
                                 "PERIOD"           =>  VARS::post("PERIOD"),       //校時
                                 "STAFF"            =>  VARS::post("STAFF"),        //担当者
                                 "SCHOOLING_SEQ"    =>  VARS::post("SCHOOLING_SEQ"),//回数
                                 "PARTNER_SCHOOL"   =>  VARS::post("PARTNER_SCHOOL"),//出席校
                                 "REMARK"           =>  VARS::post("REMARK"),       //備考
                                 "SCHREGNO"         =>  VARS::post("SCHREGNO"),     //学籍番号
                                 "NAME_KANA"        =>  VARS::post("NAME_KANA"),    //氏名（かな）
                                 "GRADE"            =>  VARS::post("GRADE")         //学年
                         );
        }
        $this->semester = 0;                //データ更新時に使用
        if ($this->control["学期数"] == 3){
            if ($this->control["学期開始日付"]["1"] <= $this->Date && $this->control["学期終了日付"]["1"] >= $this->Date) $this->semester = 1;
            if ($this->control["学期開始日付"]["2"] <= $this->Date && $this->control["学期終了日付"]["2"] >= $this->Date) $this->semester = 2;
            if ($this->control["学期開始日付"]["3"] <= $this->Date && $this->control["学期終了日付"]["3"] >= $this->Date) $this->semester = 3;              //データ更新時に使用
        }else {
            if ($this->control["学期開始日付"]["1"] <= $this->Date && $this->control["学期終了日付"]["1"] >= $this->Date) $this->semester = 1;
            if ($this->control["学期開始日付"]["2"] <= $this->Date && $this->control["学期終了日付"]["2"] >= $this->Date) $this->semester = 2;
        }
        //年度内チェック：エラー時は、年度と学期を今年度・今学期にする。
        if ($this->semester == 0){
            $this->Date  = str_replace("-","/",CTRL_DATE);
            $this->semester = CTRL_SEMESTER;
            $this->setWarning("MSG915","(年度範囲外)");
            return false;
        }
        if ($this->Date > str_replace("-","/",CTRL_DATE)){
            $this->Date  = str_replace("-","/",CTRL_DATE);
            $this->semester = CTRL_SEMESTER;
            $this->setWarning("MSG915","(処理日付がオーバーしています)");
            return false;
        }

        if ($this->cmd != "update" && $this->cmd != "add" && $this->cmd != "alldel" && $this->cmd != "chdel"){
            $this->setdata = array();
        }else {
            for ($ini = 0 ; $ini < $this->schcntall ; $ini++){
                $this->setdata["DELCHK"][$ini] = VARS::post("DELCHK".$ini);
                $this->setdata["SCHREGNO2"][$ini] = VARS::post("SCHREGNO2".$ini);
                $this->setdata["NAME"][$ini] = VARS::post("NAME".$ini);
                $this->setdata["T_TIME"][$ini] = VARS::post("T_TIME".$ini);
                $this->setdata["REMARK2"][$ini] = VARS::post("REMARK2".$ini);
            }
        }

        if ($this->field["GRADE"]) 
            $this->field["GRADE"] = sprintf("%02d",$this->field["GRADE"]);

        $this->getPropertiesAll(); //プロパティファイルの読込み
    }

    //入力項目チェック
    function validateInput()
    {
        if (VARS::request("cmd") == 'add'){
            if (!$this->field["SCHREGNO"]) {
                $this->setWarning("学籍番号を入力して下さい。");
                return false;
            }
            //チェック用データ取得
            $db = Query::dbCheckOut();
            $query = knjm390Query::getCheckout($this);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (!$row){
                $this->setWarning("講座に登録されていない学籍番号です");
                return false;
            }
            Query::dbCheckIn($db);
            if (!$this->field["SCHOOLING_SEQ"] && VARS::post("COURSE") == 1) {
                $this->setWarning("回数設定がされていません");
                return false;
            }
            if (strlen($this->field["REMARK"]) > 60) {
                $this->setWarning("MSG915",strlen($this->field["REMARK"])."(備考は60バイトまでです)");
                return false;
            }
            if (VARS::post("COURSE") == 1) {
                $db = Query::dbCheckOut();
                $query = knjm390Query::getCheckIns($this);
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if ($row){
                    $this->setWarning("既に登録されています。");
                    return false;
                }
                Query::dbCheckIn($db);
            }
        }
        return true;
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            if (VARS::request("cmd") == 'add'){
                $this->insert();
            }else if(VARS::request("cmd") == 'update'){
                $this->update();
            }else if(VARS::request("cmd") == 'alldel'){
                $this->delete();
            }else {
                $this->delback = array();
                $delc = 0;
                for ($i = 0;$i < $this->schcntall;$i++){
                    if ($this->setdata["DELCHK"][$i]=="on"){
                        $this->delback[$delc] = $this->setdata["SCHREGNO2"][$i];
                        $delc++;
                    }
                }
                $this->schnodel = "";
                for ($i = 0;$i < get_count($this->delback);$i++){
                    if ($i == 0){
                        $this->schnodel .= "(";
                    }else {
                        $this->schnodel .= ",";
                    }
                    $this->schnodel .= "'".$this->delback[$i]."'";
                }
                if (get_count($this->delback) != 0){
                    $this->schnodel .= ")";
                }else {
                    $this->setWarning("削除データを選択して下さい。");
                    return false;
                }
                $this->deletesitei();
            }

        }
    }

    /**
     * インサート時の内部メソッド
     */
    function &insert()
    {
        knjm390Query::getInsertQuery($this);
        if (VARS::post("COURSE") == 1) {
            knjm390Query::getUpdateSchchr($this);
        }
        //出席校
        if ($this->Properties["usePartnerSchool"] == "1") {
            knjm390Query::getInsertPartnerSchool($this);
        }
    }
    /**
     * アップデート時の内部メソッド
     */
    function &update()
    {
        knjm390Query::getUpdateQuery($this);
    }
    /**
     * デリート時の内部メソッド
     */
    function &delete()
    {
        knjm390Query::getDeleteQuery($this);
    }
    function &deletesitei()
    {
        knjm390Query::getDeleteQuerysitei($this);
    }

}
?>

<?php

require_once('for_php7.php');


class knja110_2Model extends Model {
    var $field    = array();
    var $cmd;
    var $schregno;          //左画面よりgetで取得
    var $issuedate = "";    //Form1.phpよりgetで取得(有効期間開始日付：YYYY-MM-DD)
    var $no;                //Form1.phpよりgetで取得(学籍番号)

    function knja110_2Model(){

    }

    function init()
    {
        $this->field = array("ISSUEDATE"        => str_replace("/","-",VARS::post("ISSUEDATE")),
                             "EXPIREDATE"       => str_replace("/","-",VARS::post("EXPIREDATE")),
                             "ZIPCD"            => VARS::post("ZIPCD"),
                             "AREACD"           => VARS::post("AREACD"),
                             "ADDR1"            => VARS::post("ADDR1"),
                             "ADDR2"            => VARS::post("ADDR2"),
                             "ADDR1_ENG"        => VARS::post("ADDR1_ENG"),
                             "ADDR2_ENG"        => VARS::post("ADDR2_ENG"),
                             "TELNO"            => VARS::post("TELNO"),
                             "FAXNO"            => VARS::post("FAXNO"),
                             "EMAIL"            => VARS::post("EMAIL"),

                            /*コメント2005/10/20 */
#                             "EMERGENCYCALL"    => VARS::post("EMERGENCYCALL"),
#                             "EMERGENCYTELNO"   => VARS::post("EMERGENCYTELNO"),
                            /*コメントおわり */

                             "RELATIONSHIP"     => VARS::post("RELATIONSHIP"),
                             "GUARD_NAME"       => VARS::post("GUARD_NAME"),
                             "GUARD_KANA"       => VARS::post("GUARD_KANA"),
                             "GUARD_SEX"        => VARS::post("GUARD_SEX"),
                             "GUARD_BIRTHDAY"   => str_replace("/","-",VARS::post("GUARD_BIRTHDAY")),
                             "GUARD_ZIPCD"      => VARS::post("GUARD_ZIPCD"),
                             "GUARD_ADDR1"      => VARS::post("GUARD_ADDR1"),
                             "GUARD_ADDR2"      => VARS::post("GUARD_ADDR2"),
                             "GUARD_TELNO"      => VARS::post("GUARD_TELNO"),
                             "GUARD_FAXNO"      => VARS::post("GUARD_FAXNO"),
                             "GUARD_E_MAIL"     => VARS::post("GUARD_E_MAIL"),
                             "GUARD_JOBCD"      => VARS::post("GUARD_JOBCD"),
                             "GUARD_WORK_NAME"  => VARS::post("GUARD_WORK_NAME"),
                             "GUARD_WORK_TELNO" => VARS::post("GUARD_WORK_TELNO"),

                             /* 2005/10/20 追加 */
                             "EMERGENCYCALL"       => VARS::post("EMERGENCYCALL"),
                             "EMERGENCYNAME"       => VARS::post("EMERGENCYNAME"),
                             "EMERGENCYRELA_NAME"  => VARS::post("EMERGENCYRELA_NAME"),
                             "EMERGENCYTELNO"      => VARS::post("EMERGENCYTELNO"),
                             "EMERGENCYCALL2"      => VARS::post("EMERGENCYCALL2"),
                             "EMERGENCYNAME2"      => VARS::post("EMERGENCYNAME2"),
                             "EMERGENCYRELA_NAME2" => VARS::post("EMERGENCYRELA_NAME2"),
                             "EMERGENCYTELNO2"     => VARS::post("EMERGENCYTELNO2"),
                             /* 2005/10/20 追加おわり */

                             "UPDATED"          => VARS::post("UPDATED"));

        $this->cmd            = VARS::request("cmd");

        if(VARS::get("SCHREGNO")){
            $this->schregno  = VARS::get("SCHREGNO");
        }
        if(VARS::get("ISSUEDATE")){
            $this->issuedate  = VARS::get("ISSUEDATE");
        }
        if ($this->cmd == ""){
            unset($this->issuedate);
        }
    }

    //入力項目チェック
    function validateInput()
    {
        //更新,削除時にリストから選択しているかをチェック
        if($this->cmd == "update"){
            if($this->issuedate ==""){
                $this->setWarning("MSG308");
                return false;
            }
        }
        //追加、更新時の必須項目(有効期間)チェック
        if($this->cmd =="add" || $this->cmd =="update" ){
            if($this->field["ISSUEDATE"] =="" || $this->field["EXPIREDATE"] ==""){
                $this->setWarning("MSG301","( 有効期間 )");
                return false;
            }
        }

        //更新日付チェック
        if($this->cmd =="update" || $this->cmd =="delete"){
            $db = Query::dbCheckOut();
            $query = knja110_2Query::getRow_Address($this->schregno,$this->issuedate);
            $row = $db->getRow($query,DB_FETCHMODE_ASSOC);
            Query::dbCheckIn($db);

            if($row["UPDATED"] != $this->field["UPDATED"]){
                $this->setWarning("MSG307");
                return false;
            }
        }

        //入力時の文字数,半角カナチェック
        foreach(    array("ZIPCD"          => array("( 郵便番号 )", 8),
                          "ADDR1"          => array("( 住所 )", 90),
                          "ADDR2"          => array("( 方書き )", 90),
                          "ADDR1_ENG"      => array("( 英字 住所 )", 70),
                          "ADDR2_ENG"      => array("( 英字 方書き )", 70),
                          "TELNO"          => array("( 電話番号 )",14),
                          "FAXNO"          => array("( FAX番号 )", 14),
                          "EMAIL"          => array("( E-Mail )", 20),

                             /* 2005/10/20 コメント追加 */
#                          "EMERGENCYCALL"  => array("( 急用連絡先 )", 60),
#                          "EMERGENCYTELNO" => array("( 急用電話番号 )", 14),
                             /* 2005/10/20 コメントおわり */

                          "GUARD_NAME"     => array("( 保護者氏名 )", 60),
                          "GUARD_KANA"     => array("( 保護者かな )", 120),
                          "GUARD_ZIPCD"    => array("( 郵便番号 )", 8),
                          "GUARD_ADDR1"    => array("( 保護者住所 )", 90),
                          "GUARD_ADDR2"    => array("( 保護者方書き )", 90),
                          "GUARD_TELNO"    => array("( 電話番号 )",14),
                          "GUARD_FAXNO"    => array("( FAX番号 )", 14),
                          "GUARD_E_MAIL"    => array("( E-Mail )", 20),
                          "GUARD_WORK_NAME" => array("( 勤務先名称 )", 60),
                          "GUARD_TELNO"    => array("( 勤務先電話番号 )",14),


                          /* 2005/10/20 追加 */
                          "EMERGENCYCALL"        => array("( 急用連絡先 )",60),
                          "EMERGENCYNAME"        => array("( 急用連絡氏名 )",60),
                          "EMERGENCYRELA_NAME"   => array("( 急用連絡続柄名 )",30),
                          "EMERGENCYTELNO"       => array("( 急用電話番号 )",14),
                          "EMERGENCYCALL2"       => array("( 急用連絡先２ )",60),
                          "EMERGENCYNAME2"       => array("( 急用連絡氏名２ )",60),
                          "EMERGENCYRELA_NAME2"  => array("( 急用連絡続柄名２ )",30),
                          "EMERGENCYTELNO2"      => array("( 急用電話番号２ )",14)
                          /* 2005/10/20 追加おわり */

                          ) as $key => $val){

            if (mbereg("[｡-ﾟ]",$this->field[$key])) {
                $this->setWarning("MSG901",$val[0]."半角カナは入力できません");
                return false;
            }
            if(strlen($this->field[$key]) > $val[1]){
                $this->setWarning("MSG915", $val[0].$val[1]."バイトまで");
                return false;
            }
        }

        //有効期間開始日付と有効期間終了日付の日付の前後関係をチェック
        $ISSUEDATE = strtotime(str_replace("-","/",$this->field["ISSUEDATE"]));
        $EXPIREDATE = strtotime(str_replace("-","/",$this->field["EXPIREDATE"]));
        if(($EXPIREDATE <= $ISSUEDATE) && ($this->cmd =="add" || $this->cmd =="update")){
            $this->setWarning("MSG916","( 有効期間 )");
            return false;
        }
        //追加,更新時の有効期間の重複チェック
        if($this->field["ISSUEDATE"] !="")
        {
            $db = Query::dbCheckOut();
            $result = $db->query(knja110_2Query::getAddress_all($this));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                if(($row["ISSUEDATE"] == str_replace("/","-",$this->issuedate)) &&
                 ($this->cmd =="update"))
                    continue;

                $issuedate = strtotime(str_replace("-","/",$row["ISSUEDATE"]));
                $expiredate = strtotime(str_replace("-","/",$row["EXPIREDATE"]));

                if (($issuedate <= $ISSUEDATE && $ISSUEDATE <= $expiredate)
                    || ($issuedate <= $EXPIREDATE && $EXPIREDATE <= $expiredate) ||
                    ($ISSUEDATE <= $issuedate && $expiredate <= $EXPIREDATE)) {
                        $this->setWarning("MSG302","( 有効期間 )");
                        return false;
                }
            }
            $result->free();
            Query::dbCheckIn($db);
        }
        //文字型チェック
        foreach(array("TELNO"           => array("[^-|0-9]",        "( 電話番号 )"),
                     "FAXNO"            => array("[^-|0-9]",        "( FAX番号 )"),
                     "EMAIL"            => array("[^-._@a-z0-9]",   "( E-Mail )"),

                     "EMERGENCYTELNO"   => array("[^-|0-9]",        "( 急用電話番号 )"),
                     "EMERGENCYTELNO2"   => array("[^-|0-9]",       "( 急用電話番号２ )"),

                     "GUARD_TELNO"      => array("[^-|0-9]",        "( 電話番号 )"),
                     "GUARD_FAXNO"      => array("[^-|0-9]",        "( FAX番号 )"),
                     "GUARD_E_MAIL"     => array("[^-._@a-z0-9]",   "( E-Mail )"),
                     "GUARD_WORK_TELNO" => array("[^-|0-9]",        "( 勤務先電話番号 )"),
                  ) as $key => $val){

            if($this->field[$key] !="" && preg_match('{' . $val[0] . '}i', trim($this->field[$key]))){
                $this->setWarning("MSG901", $val[1]);
                return false;
            }
        }
        /*
            C -- 全角ひらがなを全角カタカナ
            K -- 半角カナを全角カタカナ
            k -- 全角カタカナを半角カタカナ
            A -- 英数字を半角から全角
        */
        //(英字)住所1チェック
        if($this->field["ADDR1_ENG"] !="")
        {
            if(trim($this->field["ADDR1_ENG"]) != i18n_ja_jp_hantozen(trim($this->field["ADDR1_ENG"]),"CK")
              || trim($this->field["ADDR1_ENG"]) != i18n_ja_jp_hantozen(trim($this->field["ADDR1_ENG"]),"k")
              || trim($this->field["ADDR1_ENG"]) == i18n_ja_jp_hantozen(trim($this->field["ADDR1_ENG"]),"A")    || trim($this->field["ADDR1_ENG"]) != i18n_ja_jp_hantozen(trim($this->field["ADDR1_ENG"]),"a"))
            {
                $this->setWarning("MSG901","\\n半角英字を入力して下さい！ ( 英字 住所 )");
                return false;
            }
        }

        //(英字)住所2チェック
        if($this->field["ADDR2_ENG"] !="")
        {
            if(trim($this->field["ADDR2_ENG"]) != i18n_ja_jp_hantozen(trim($this->field["ADDR2_ENG"]),"CK") 
              || trim($this->field["ADDR2_ENG"]) != i18n_ja_jp_hantozen(trim($this->field["ADDR2_ENG"]),"k")
              || trim($this->field["ADDR2_ENG"]) == i18n_ja_jp_hantozen(trim($this->field["ADDR2_ENG"]),"A")    || trim($this->field["ADDR2_ENG"]) != i18n_ja_jp_hantozen(trim($this->field["ADDR2_ENG"]),"a"))
            {
                $this->setWarning("MSG901","\\n半角英字を入力して下さい！ ( 英字 方書き )");
                return false;
            }
        }
        return true;
    }

    //インサート時の内部メソッド
    function &getInsertModel()
    {
        if ($this->schregno=="") {
            $this->setWarning("MSG304");
            return false;
        }
        
        //入力チェック
        if($this->validateInput())
        {
            knja110_2Query::getInsertQuery($this);
            $this->setMessage("MSG201");
            $this->issuedate = $this->field["ISSUEDATE"];
            return;
        }
    }

    //アップデート時の内部メソッド
    function &getUpdateModel()
    {
        //入力チェック
        if($this->validateInput())
        {
            knja110_2Query::getUpdateModel($this);
            $this->setMessage("MSG201");
            $this->issuedate = $this->field["ISSUEDATE"];
            return;
        }
    }
    //削除用のモデルを返します
    function &getDeleteModel()
    {
        if($this->field["ISSUEDATE"] =="" && $this->issuedate ==""){
            $this->setWarning("MSG308");
            return false;
        }
        knja110_2Query::getDeleteQuery($this);
        unset($this->field["ISSUEDATE"]);
        unset($this->issuedate);
        $this->setMessage("MSG201");
        return;
    }

     //変数をクリアします
    function clean(){}
}
?>

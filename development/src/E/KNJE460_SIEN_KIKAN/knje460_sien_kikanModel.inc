    <?php

require_once('for_php7.php');

class knje460_sien_kikanModel extends Model {
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

    var $sien_moji;  //支援内容の文字数
    var $sien_gyou;  //支援内容の行数
    var $hikitsugi_moji;  //引継事項の文字数
    var $hikitsugi_gyou;  //引継事項の行数

    function knje460_sien_kikanModel() {
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

        $this->field5 = array("YEAR"                =>  VARS::post("YEAR"),             //更新年度
                              "PASTYEAR"            =>  VARS::post("PASTYEAR"),         //過年度
                              "PASTYEARLOADFLG"     =>  VARS::post("HID_PASTYEARLOADFLG"), //過年度読込みフラグ
                              "ENTRANT_NAME"        =>  VARS::post("ENTRANT_NAME"),     //記入者
                              "SIEN_KIKAN"          =>  VARS::post("SIEN_KIKAN"),       //関係機関
                              "SIEN_KIKAN_COUNT"    =>  VARS::post("SIEN_KIKAN_COUNT"), //関係機関数
                              "SELECT_COUNT"        =>  VARS::post("SELECT_COUNT"),     //項目数
                              "HIKITSUGI"           =>  VARS::post("HIKITSUGI")         //引継事項
                             );

        //関係機関の格納
        for ($re = 1; $re <= $this->field5["SIEN_KIKAN_COUNT"]; $re++) {
            $this->field5["SIEN_KIKAN".$re] = VARS::post("SIEN_KIKAN".$re); //関係機関
            $sienKikan = $this->field5["SIEN_KIKAN".$re];
            for ($idx = 1; $idx <= $this->field5["SELECT_COUNT"]; $idx++) {
                $this->field5["SIEN_".$sienKikan.$idx]       = VARS::post("SIEN_".$sienKikan.$idx);       //支援内容
                $this->field5["STATUS_".$sienKikan.$idx]     = VARS::post("STATUS_".$sienKikan.$idx);     //次年度
            }
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
        //支援内容の文字数と行数の設定
        $this->sien_moji = 43;
        $this->sien_gyou = 2;

        //引継事項の文字数と行数の設定
        $this->hikitsugi_moji = 46;
        $this->hikitsugi_gyou = 4;
    }

    //更新用のモデルを返します
    function &getUpdateModel()
    {
        $db = Query::dbCheckOut();

        //更新処理
        if ($this->cmd === 'subform1_update') {
        
            //SCHREG_CHALLENGED_SUPPORTPLAN_MAIN_DATの更新
            knje460_sien_kikanQuery::getUpdateMainDat($db, $this);
        
            for ($re = 1; $re <= $this->field5["SIEN_KIKAN_COUNT"]; $re++) {
                $sienKikan = $this->field5["SIEN_KIKAN".$re];
                //DELETE
                knje460_sien_kikanQuery::getDeleteMainQuery($db, $this, $sienKikan);

                for ($idx = 1;$idx <= $this->field5["SELECT_COUNT"];$idx++) {
                    $sien   = $this->field5["SIEN_".$sienKikan.$idx];
                    $status = $this->field5["STATUS_".$sienKikan.$idx];
                    if($sien != "" || $status != ""){
                        //INSERT
                        knje460_sien_kikanQuery::getUpdateMainQuery($db, $this, $sienKikan, sprintf('%02d',$idx), $sien, $status);
                    }
                }
            }
            //INSERT
            knje460_sien_kikanQuery::getUpdateSupportPlan($db, $this, '05', '01', $this->field5["HIKITSUGI"]);
        }

        $this->setMessage("MSG201");
        Query::dbCheckIn($db);

        return;
    }


}
?>

<?php

require_once('for_php7.php');

class knja120Model extends Model {

    var $cmd;
    var $schregno = "";
    var $name = "";
    var $field = array();
    var $control;
    var $annual;                    //学年(年次)
    var $exp_year;       //左フレームから送られてくる年度
    var $exp_semester;   //左フレームから送られてくる学期
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    function knja120Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if(VARS::get("SEND_PRGID")){
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        if (VARS::get("PROGRAMID")!="") {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
        }

        if (VARS::post("ANNUAL")){   //学年（年次）
            list($tmp["YEAR"], $tmp["ANNUAL"]) = explode(",", VARS::post("ANNUAL"));
            $this->annual = $tmp;
        }

        $this->field = array("TOTALSTUDYACT"        =>  VARS::post("TOTALSTUDYACT"),
                             "TOTALSTUDYVAL"        =>  VARS::post("TOTALSTUDYVAL"),
                             "TOTALSTUDYACT2"       =>  VARS::post("TOTALSTUDYACT2"),                            
                             "TOTALSTUDYVAL2"       =>  VARS::post("TOTALSTUDYVAL2"),                            
                             "SPECIALACTREMARK"     =>  VARS::post("SPECIALACTREMARK"),
                             "TOTALREMARK"          =>  VARS::post("TOTALREMARK"),
                             "ATTENDREC_REMARK"     =>  VARS::post("ATTENDREC_REMARK"),
                             "CREDITREMARK"         =>  VARS::post("CREDITREMARK"),
                             "DAIGAE_CHECK"         =>  VARS::post("DAIGAE_CHECK"),//東京都仕様、奉仕の記録に代替で入る
                             "UPDATED"              =>  VARS::post("UPDATED")
                             );

        $this->cmd = VARS::request("cmd");

        if (VARS::get("EXP_YEAR")!="") {
            $this->exp_year     = VARS::get("EXP_YEAR");
            $this->exp_semester = VARS::get("EXP_SEMESTER");
        }
        if (VARS::get("SCHREGNO")!="") {
            $this->schregno   = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME")!="") {
            $this->name       = VARS::get("NAME");
        }
        if (VARS::get("GRADE")!="") {
            $this->grade      = VARS::get("GRADE");
        }

        //左フレームから送られてくる年度
        if (VARS::get("EXP_YEAR")) {
            $this->exp_year = VARS::get("EXP_YEAR");
        }
        //左フレームから送られてくる学期
        if (VARS::get("EXP_SEMESTER")) {
            $this->exp_semester = VARS::get("EXP_SEMESTER");
        }
        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
            unset($this->annual);
            unset($this->grade);
        }

        if (VARS::get("target") != "") {
            $this->target   = VARS::get("target");
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //年度コンボ（通知表所見）
        if ($this->Properties["useTuutisyoSyokenNendo"] == "1") {
            $this->year_cmb = VARS::request("YEAR_CMB");
        } else {
            $this->year_cmb = $this->exp_year;
        }
        
        $this->file = VARS::file("FILE");
    }

    function &getProgrampathModel()
    {
        $db = Query::dbCheckOut();
        $programpath = $db->getOne(knja120Query::getProgrampath());
        Query::dbCheckIn($db);

        return $programpath;
    }

    //入力項目チェック
    function validateInput()
    {
        if ($this->schregno=="") {
            $this->setWarning("MSG304");
            return false;
        }
        if ($this->validate_row_cnt($this->field["TOTALSTUDYACT"], (44 * 2)) > 4) {
            $this->setWarning("MSG915","(活動内容は4行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["TOTALSTUDYVAL"], (44 * 2)) > 6) {
            $this->setWarning("MSG915","(評価は6行までです)");
            return false;
        }
        
        if ($this->getname === 'tokyoto') {
            if ($this->validate_row_cnt($this->field["TOTALSTUDYACT2"], (44 * 2)) > 4) {
                $this->setWarning("MSG915","(活動内容は4行までです)");
                return false;
            }
            if ($this->validate_row_cnt($this->field["TOTALSTUDYVAL2"], (44 * 2)) > 6) {
                $this->setWarning("MSG915","(評価は6行までです)");
                return false;
            }
            if ($this->validate_row_cnt($this->field["CREDITREMARK"], (44 * 2)) > 5) {
                $this->setWarning("MSG915","(備考は5行までです)");
                return false;
            }
        }
        
        if ($this->Properties["seitoSidoYorokuSpecialactremarkFieldSize"] == 1) {
            if ($this->validate_row_cnt($this->field["SPECIALACTREMARK"], (22 * 2)) > 10) {
                $this->setWarning("MSG915","(特別活動所見は10行までです)");
                return false;
            }
        } else {
            if ($this->validate_row_cnt($this->field["SPECIALACTREMARK"], (11 * 2)) > 6) {
                $this->setWarning("MSG915","(特別活動所見は6行までです)");
                return false;
            }
        }

        if ($this->Properties["seitoSidoYorokuSougouFieldSize"] == 1) {
            if ($this->validate_row_cnt($this->field["TOTALREMARK"], (66 * 2)) > 8) {
                $this->setWarning("MSG915","(総合所見は8行までです)");
                return false;
            }
        } elseif ($this->Properties["seitoSidoYorokuFieldSize"] == 1) {
            if ($this->validate_row_cnt($this->field["TOTALREMARK"], (66 * 2)) > 7) {
                $this->setWarning("MSG915","(総合所見は7行までです)");
                return false;
            }
        } else {
            if ($this->validate_row_cnt($this->field["TOTALREMARK"], (44 * 2)) > 6) {
                $this->setWarning("MSG915","(総合所見は6行までです)");
                return false;
            }
        }
        if ($this->validate_row_cnt($this->field["ATTENDREC_REMARK"], (20 * 2)) > 2) {
            $this->setWarning("MSG915","(出欠の記録備考は2行までです)");
            return false;
        }
        return true;
    }

    function &getUpdateModel()
    {
        if (!$this->validateInput()) {
            return;
        }
        $this->update();
        $this->setMessage("MSG201");
    }

    function &update()
    {
        knja120Query::getDeleteQuery($this->schregno, $this);
        knja120Query::getDeleteHdat($this->schregno);
        knja120Query::getInsertQuery($this->field,$this->schregno, $this);
        knja120Query::getInsertHdat($this->field,$this->schregno, $this);
        return $result;
    }

    /**
     * 実行モデルを返します
    */
    function &getExecModel()
    {
        $setFileName = explode(".", $this->file['name']);
        $setFileName[0] = mb_convert_encoding($setFileName[0], "SJIS-win", "UTF-8");
        $extension = $setFileName[get_count($setFileName) - 1];
//echo "cnt = " . get_count($setFileName) . "<BR>";
//echo "extension = " . $extension . "<BR>";
        //拡張子チェック
        if ($extension != "pdf" && $extension != "PDF") {
            $this->warning = "PDFファイルを指定して下さい。\\n(".$extension."ファイルの取込は出来ません。)";
            return false;
        }
        //アップロード(PDF)
        $dirname  = DOCUMENTROOT . "/pdf/" . $this->schregno;
        $filename = $dirname . "/" . $setFileName[0] . "_" . date("Ymj_His") . '.' . $extension;
        if (!is_uploaded_file($this->file['tmp_name'])) {
            $this->warning = "ファイル名が不正です。";
        } else if ($this->schregno == "") {
            $this->warning = "学籍番号が不正です。";
        } else {
            //ディレクトリ（学籍番号）か・・・なければ作成
            if (!is_dir($dirname)) {
                if (!mkdir($dirname)) {
                    //作成失敗
                }
            }
            if (move_uploaded_file($this->file['tmp_name'], $filename) == FALSE){
                $this->warning = "取込失敗です。";
            } else {
                $this->message = "取込正常です。";
            }
        }
    }
    
}
?>

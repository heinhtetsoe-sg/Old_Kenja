<?php

require_once('for_php7.php');

class knjj183_familyModel extends Model {
    var $field    = array();
    var $cmd;
    var $schregno;          //左画面よりgetで取得
    var $no;                //Form1.phpよりgetで取得(学籍番号)

    function knjj183_familyModel() {

    }

    function init() {
        $this->field = array("RELA_NO"                  => VARS::post("RELA_NO"),
                             "RELA_NAME"                => VARS::post("RELA_NAME"),
                             "RELA_KANA"                => VARS::post("RELA_KANA"),
                             "RELA_SEX"                 => VARS::post("RELA_SEX"),
                             "RELA_BIRTHDAY"            => VARS::post("RELA_BIRTHDAY"),
                             "RELA_RELATIONSHIP"        => VARS::post("RELA_RELATIONSHIP"),
                             "RELA_OCCUPATION"          => VARS::post("RELA_OCCUPATION"),
                             "RELA_SCHREGNO"            => VARS::post("RELA_SCHREGNO"),
                             "REGD_GRD_FLG"             => VARS::post("REGD_GRD_FLG"),
                             "RELA_GRADE"               => VARS::post("RELA_GRADE"),
                             "RELA_REGIDENTIALCD"       => VARS::post("RELA_REGIDENTIALCD"),
                             "TYOUSHI_FLG"              => VARS::post("TYOUSHI_FLG"),
                             "RELA_REMARK"              => VARS::post("RELA_REMARK"),

                             "UPDATED"              => VARS::post("UPDATED"),
                             "RELA_UPDATED"         => VARS::post("RELA_UPDATED"),
                             );

        $this->cmd            = VARS::request("cmd");

        if(VARS::get("SCHREGNO")){
            $this->schregno  = VARS::get("SCHREGNO");
        }
        if(VARS::get("RELA_NO")) {
            $this->rela_no  = VARS::get("RELA_NO");
        }
        if ($this->cmd == ""){
            unset($this->rela_no);
        }
        //兄弟データの元の学籍番号
        $this->stucd = VARS::request("STUCD");

        $this->getPropertiesAll(); //プロパティファイルの読込み
    }

    //入力項目チェック
    function validateInput() {
        if ($this->cmd == "addFamily") {
            if (trim($this->field["RELA_NAME"]) == "") {
                $this->setWarning("MSG301","( 氏名 )");
                return false;
            } elseif (trim($this->field["RELA_KANA"]) == "") {
                $this->setWarning("MSG301","( 氏名かな )");
                return false;
            }
        }
        //更新,削除時にリストから選択しているかをチェック
        if ($this->cmd == "updFamily") {
            if ($this->rela_no == "") {
                $this->setWarning("MSG308");
                return false;
            }
        }
        $checkdata = array( "RELA_NAME"             => array("( 氏名 )",            60),
                            "RELA_KANA"             => array("( 氏名かな )",        120),
                            "RELA_OCCUPATION"       => array("( 職業又は学校 )",    60),
                            "RELA_SCHREGNO"         => array("( 兄弟姉妹学籍番号 )",8),
                            "RELA_REMARK"           => array("( 備考 )",            45)
                            );
        //入力時の文字数,半角カナチェック
        foreach($checkdata as $key => $val) {
            if (mbereg("[｡-ﾟ]", $this->field[$key])) {
                $this->setWarning("MSG901",$val[0]."半角カナは入力できません");
                return false;
            }
            if (strlen($this->field[$key]) > $val[1]) {
                $this->setWarning("MSG915", $val[0].$val[1]."バイトまで");
                return false;
            }
        }

        return true;
    }

    //インサート時の内部メソッド
    function &getInsertModel() {
        if ($this->schregno=="") {
            $this->setWarning("MSG304");
            return false;
        }
        //入力チェック
        if($this->validateInput()) {
            if (!$this->familyNo) {
                $this->familyNo = knjj183_familyQuery::getMaxFamilyNo($this);
            }
            knjj183_familyQuery::addSchregDetail009($this, $this->schregno, $this->familyNo);
            $rela_max = knjj183_familyQuery::getFamily_Maxno($this);
            $rela_no = (($rela_max == "") ? 1 : ($rela_max + 1));
            knjj183_familyQuery::insertFamilyData($this, $this->field, $rela_no);
            $this->setMessage("MSG201");
            $this->rela_no = $rela_no;

            return;
        }
    }

    //アップデート時の内部メソッド
    function &getUpdateModel() {
        //入力チェック
        if($this->validateInput()) {
            //家族情報データ更新
            knjj183_familyQuery::updateFamilyData($this);
            $this->setMessage("MSG201");

            return;
        }
    }
    //削除用のモデルを返します
    function &getDeleteModel() {
        if ($this->rela_no == "") {
            $this->setWarning("MSG308");
            return false;
        }
        knjj183_familyQuery::deleteFamilyData($this);
        unset($this->rela_no);
        $this->setMessage("MSG201");

        return;
    }

     //変数をクリアします
    function clean(){}

    //反映処理
    function &getApplyModel() {
        $getSearchVal = VARS::post("searchVal");
        $dataArray = explode(",", $getSearchVal);
        $datas = array();
        $cnt = 0;
        foreach ($dataArray as $key => $val) {
            list($familyNo, $schreg, $hrName, $detaUmu, $name, $kana, $sex, $gh, $tel, $birthDay) = explode(" | ", $val);
            list($grade, $hrClass) = explode("_", $gh);

            //&nbsp;を変換
            $familyNo = trim($familyNo, chr(0xC2).chr(0xA0));

            $datas[$cnt]["RELA_NAME"]            = $name;
            $datas[$cnt]["RELA_KANA"]            = $kana;
            $datas[$cnt]["RELA_SEX"]             = $sex == "男" ? "1" : "2";
            $datas[$cnt]["RELA_BIRTHDAY"]        = str_replace("/","-",$birthDay);
            $datas[$cnt]["RELA_SCHREGNO"]        = $schreg;
            $datas[$cnt]["REGD_GRD_FLG"]         = "1";
            $datas[$cnt]["RELA_GRADE"]           = $grade;
            $cnt++;
            $this->familyNo = $this->familyNo ? $this->familyNo : $familyNo;
        }
        if (!$this->familyNo) {
            $this->familyNo = knjj183_familyQuery::getMaxFamilyNo($this);
        }
        //自身のFamilyDat更新用データ
        $datas[$cnt] = knjj183_familyQuery::getSetFamilyData($this);
        foreach ($datas as $key => $fields) {
            //DETAIL009
            knjj183_familyQuery::addSchregDetail009($this, $fields["RELA_SCHREGNO"], $this->familyNo);

            //FAMILY_DAT追加
            $rela_max = knjj183_familyQuery::getFamily_Maxno($this);
            $rela_no = (($rela_max == "") ? 1 : ($rela_max + 1));
            knjj183_familyQuery::insertFamilyData($this, $fields, $rela_no);
        }
        $this->setMessage("MSG201");
    }
}
?>

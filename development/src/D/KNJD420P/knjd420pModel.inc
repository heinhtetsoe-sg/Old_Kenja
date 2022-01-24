<?php

require_once('for_php7.php');

class knjd420pModel extends Model {
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
    var $selKindNo;     //別画面に引き継ぐKIND_NO
    var $upddate;       //右フレームで設定する更新日
    var $getSort = "";
    var $sort;

    function knjd420pModel() {
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
            unset($this->selKindNo);
            unset($this->upddate);
            unset($this->updateIndex);
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
        //選択したボタンID
        if (VARS::post("HID_SELKINDNO")) {
            $this->selKindNo = VARS::post("HID_SELKINDNO");
        }
        //更新日(別画面に引き渡す値。コンボで選択した日付)
        if (VARS::post("UPDDATE")) {
            $this->upddate = VARS::post("UPDDATE");
        }
        //新規を選択した際の日付(入力 or ポップアップで指定した日付)
        if (VARS::post("SELNEWDATE")) {
            $this->selnewdate = VARS::post("SELNEWDATE");
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->selKindNo);
        }

        $this->field = array(
            "SEMESTER" => VARS::post("SEMESTER"),
        );

        //各項目のキー名称
        $this->title_key = array(
            "001" => "GOALS",
            "002" => "ACCOMMODATION",
            "003" => "KEY_GOALS",
            "004" => "GOALS_REASON",
            "005" => "LONG_GOALS",
            "006" => "SHORT_GOALS",
            "007" => "MEANS",
            "008" => "EVALUATION",
            "009" => "REMARK_1",
            "010" => "REMARK_2",
            "011" => "REMARK_3",
            "012" => "REMARK_4"
        );

        //各項目のキー名称
        $this->title_default = array(
            $this->title_key["001"] => "支援計画の目標",
            $this->title_key["002"] => "配慮事項",
            $this->title_key["003"] => "重点目標",
            $this->title_key["004"] => "目標設定理由",
            $this->title_key["005"] => "長期目標",
            $this->title_key["006"] => "短期目標",
            $this->title_key["007"] => "手立て　学習場面",
            $this->title_key["008"] => "評価",
            $this->title_key["009"] => "わかる",
            $this->title_key["010"] => "かかわり",
            $this->title_key["011"] => "からだ",
            $this->title_key["012"] => "けんこう"
        );

        //各textareaの最大文字数
        $this->textLimit = array(
            $this->title_key["001"] => array("moji" => 50, "gyou" => 3),
            $this->title_key["002"] => array("moji" => 50, "gyou" => 3),
            $this->title_key["003"] => array("moji" => 20, "gyou" => 6),
            $this->title_key["004"] => array("moji" => 20, "gyou" => 6),
            $this->title_key["005"]."1" => array("moji" => 8, "gyou" => 24),
            $this->title_key["006"]."1" => array("moji" => 16, "gyou" => 24),
            $this->title_key["007"]."1" => array("moji" => 16, "gyou" => 24),
            $this->title_key["008"]."1" => array("moji" => 16, "gyou" => 24),
            $this->title_key["005"]."2" => array("moji" => 8, "gyou" => 24),
            $this->title_key["006"]."2" => array("moji" => 16, "gyou" => 24),
            $this->title_key["007"]."2" => array("moji" => 16, "gyou" => 24),
            $this->title_key["008"]."2" => array("moji" => 16, "gyou" => 24),
            $this->title_key["005"]."3" => array("moji" => 8, "gyou" => 24),
            $this->title_key["006"]."3" => array("moji" => 16, "gyou" => 24),
            $this->title_key["007"]."3" => array("moji" => 16, "gyou" => 24),
            $this->title_key["008"]."3" => array("moji" => 16, "gyou" => 24),
            $this->title_key["005"]."4" => array("moji" => 8, "gyou" => 24),
            $this->title_key["006"]."4" => array("moji" => 16, "gyou" => 24),
            $this->title_key["007"]."4" => array("moji" => 16, "gyou" => 24),
            $this->title_key["008"]."4" => array("moji" => 16, "gyou" => 24)
        );

        //更新対象のSEQ
        if (VARS::post("UPDATE_INDEX")) {
            $this->updateIndex = VARS::post("UPDATE_INDEX");
        }

        //タイトル
        for ($i = 1; $i <= 12; $i++) {
            $key = sprintf("%03d", $i);
            $key = "INIT_".$this->title_key[$key]."_TITLE";
            $this->field[$key] = (VARS::post($key)) ? VARS::post($key) : "" ;
        }

        //項目値
        for ($i = 1; $i <= 4; $i++) {
            $key = sprintf("%03d", $i);
            $key = $this->title_key[$key];
            $this->field[$key] = (VARS::post($key)) ? VARS::post($key) : "" ;
        }
        for ($i = 1; $i <= 4; $i++) {
            for ($j = 5; $j <= 8; $j++) {
                $key = sprintf("%03d", $j);
                $key = $this->title_key[$key].$i;
                $this->field[$key] = (VARS::post($key)) ? VARS::post($key) : "" ;
            }
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }

        //文字数チェック
        if ($this->cmd == "update") {
            switch (intval($this->updateIndex)) {
                case 1 :
                    //支援計画の目標・配慮事項
                    for ($i = 1; $i <= 2; $i++) {
                        $key = sprintf("%03d", $i);
                        $key = $this->title_key[$key];
                        if ($this->validate_row_cnt($this->field[$key], ($this->textLimit[$key]["moji"] * 2)) > $this->textLimit[$key]["gyou"]) {
                            $this->setWarning("MSG915","({$this->field["INIT_".$key."_TITLE"]}は{$this->textLimit[$key]["gyou"]}行までです)");
                            return false;
                        }
                    }
                    break;
                case 2 :
                    //重点目標・目標設定理由
                    for ($i = 3; $i <= 4; $i++) {
                        $key = sprintf("%03d", $i);
                        $key = $this->title_key[$key];
                        if ($this->validate_row_cnt($this->field[$key], ($this->textLimit[$key]["moji"] * 2)) > $this->textLimit[$key]["gyou"]) {
                            $this->setWarning("MSG915","({$this->field["INIT_".$key."_TITLE"]}は{$this->textLimit[$key]["gyou"]}行までです)");
                            return false;
                        }
                    }
                    break;
                case 3 :
                    //横項目・縦項目
                    for ($i = 1; $i <= 4; $i++) {
                        $key = sprintf("%03d", $i+8);
                        $key = $this->title_key[$key];
                        $title1 = $this->field["INIT_".$key."_TITLE"];
                        for ($j = 5; $j <= 8; $j++) {
                            $key = sprintf("%03d", $j);
                            $key = $this->title_key[$key];
                            $title2 = $this->field["INIT_".$key."_TITLE"];
                            $title = $title1."(".$title2.")";
                            if ($this->validate_row_cnt($this->field[$key.$i], ($this->textLimit[$key.$i]["moji"] * 2)) > $this->textLimit[$key.$i]["gyou"]) {
                                $this->setWarning("MSG915","({$title}は{$this->textLimit[$key.$i]["gyou"]}行までです)");
                                return false;
                            }
                        }
                    }
                    break;
                default :
                    //処理なし
            }
        }

        return true;
    }

    //更新用のモデルを返します
    function &getUpdateModel() {
        if (!$this->validateInput()) {
            return;
        }
        knjd420pQuery::getInsertQuery($this);
        $this->setMessage("MSG201");
    }

}
?>

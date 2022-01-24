<?php

require_once('for_php7.php');

class knjx_hexam_remarkModel extends Model {
    var $cmd;
    var $schregno;      //学籍番号
    var $name;          //氏名
    var $grade;         //学年
    var $hrClass;       //クラス
    var $annual;        //学年(年次)
    var $control;
    var $mode;          //在籍卒業モード
    var $exp_year;      //呼び出し画面から送られてくる年度
    var $exp_semester;  //呼び出し画面から送られてくる学期
    var $schoolName;
    var $schoolCode;

    var $remark_moji;    //備考の文字数
    var $remark_gyou;    //備考の行数
    var $remark2_gyou;   //備考2の行数

    var $no_comments;           //チェック（特記事項なし）
    var $no_comments_label;     //文言（特記事項なし）

    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    var $data = array();    //選択された生徒一覧
    var $school_kind;

    function knjx_hexam_remarkModel() {
        $this->year = CTRL_YEAR;
        $this->mode = "ungrd";      //在籍モード
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->hrClass);
        }

        $this->cmd     = VARS::request("cmd");

        if(VARS::get("SEND_PRGID")){
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
            unset($this->data);
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        //年度
        if (VARS::get("EXP_YEAR")) {
            $this->exp_year = VARS::get("EXP_YEAR");
        }
        //学期
        if (VARS::get("EXP_SEMESTER")) {
            $this->exp_semester = VARS::get("EXP_SEMESTER");
        }

        //表示科目
        if (VARS::get("SCHREGNO")) {
            $this->schregno = VARS::get("SCHREGNO"); //学籍番号
        }
        if (VARS::get("NAME")) {
            $this->name = VARS::get("NAME");         //生徒氏名
        }
        if (VARS::get("GRADE")) {
            $this->grade = VARS::get("GRADE");       //学年
        }
        if (VARS::get("HR_CLASS")) {
            $this->hrClass = VARS::get("HR_CLASS");  //クラス
        }

        //在籍卒業モード
        if (VARS::get("mode")) {
            $this->mode = VARS::get("mode");         //在籍卒業モード
        }

        if ($this->cmd == "update") {
            $this->data["selectdata"]  = VARS::post("selectdata");
            $this->data["field"] = array();
            $this->data["check"] = array("RCHECK0"           => VARS::post("RCHECK0"),
                                                 "RCHECK1"           => VARS::post("RCHECK1"),
                                                 "RCHECK2"           => VARS::post("RCHECK2"));
            $this->data["check_all"] = VARS::post("RCHECK3");

            if (VARS::post("RCHECK0") =="1") $this->data["field"]["REMARK"]            = VARS::post("REMARK");
            if (VARS::post("RCHECK0") =="1") $this->data["field"]["NO_COMMENTS"]       = VARS::post("NO_COMMENTS");
            if (VARS::post("RCHECK1") =="1") $this->data["field"]["REMARK2"]           = VARS::post("REMARK2");
            if (VARS::post("RCHECK2") =="1") $this->data["field"]["COMMENTEX_A_CD"]    = VARS::post("COMMENTEX_A_CD");
        }

        //学校名取得
        $query = knjx_hexam_remarkQuery::getSchoolName();
        $db = Query::dbCheckOut();
        $rowZ010 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $this->schoolName = $rowZ010["NAME1"];
        $this->schoolCode = $rowZ010["NAME2"];
        //校種取得
        $this->school_kind  = $db->getOne(knjx_hexam_remarkQuery::getSchoolKind($this));
        Query::dbCheckIn($db);

        //「特記事項なし」チェックボックス
        $this->no_comments_label = "特記事項なし";
        $this->no_comments = VARS::post("NO_COMMENTS");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        //固定
        $this->Properties["sidouyourokuSansyou"] = ""; //1 ⇒ E010A それ以外は E010

        $this->getFieldSize();
    }

    function &updateModel() {
        //生徒が選択されてない場合
        if (!strlen($this->data["selectdata"])) {
            $this->setCmd("main");
            $this->setWarning("MSG304");
            return false;
        }
        
        //最低ひとつでもチェックがついてる場合のみ更新
        if (!in_array("1",$this->data["check"])) {
            $this->setCmd("main");
            $this->setWarning("更新する項目にチェックをつけてください");
            return false;
        }

        //入力チェック
        if ($this->validate_row_cnt($this->data["field"]["REMARK"], ($this->remark_moji * 2)) > $this->remark_gyou) {
            $this->setCmd("main");
            $this->setWarning("MSG915","(備考は{$this->remark_gyou}行までです)");
            return false;
        }

        if ($this->validate_row_cnt($this->data["field"]["REMARK2"], ($this->remark_moji * 2)) > $this->remark2_gyou) {
            $this->setCmd("main");
            $this->setWarning("MSG915","(備考2は{$this->remark2_gyou}行までです)");
            return false;
        }

        //更新
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        knjx_hexam_remarkQuery::update($db, $this);
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        $this->setMessage("MSG201");
    }

    function getFieldSize() {
        //備考の文字数と行数の設定
        if ($this->Properties["tyousasyoRemarkFieldSize"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["tyousasyoRemarkFieldSize"]);
            $this->remark_moji = (int)trim($moji);
            $this->remark_gyou = (int)trim($gyou);
        } else {
            $this->remark_moji = 41; //デフォルトの値
            $this->remark_gyou = 5;  //デフォルトの値
        }
        
        //備考2の行数の設定
        $this->remark2_gyou = 3;  //3行固定
    }
}
?>

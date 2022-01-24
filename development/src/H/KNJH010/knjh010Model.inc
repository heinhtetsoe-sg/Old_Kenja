<?php

require_once('for_php7.php');
class knjh010Model extends Model {

    var $field = array();
    var $cmd;
    var $schregno;    //学籍番号
    var $name_show;   //生徒の名前
    var $year;        //処理年度
    var $sc;          //セキュリティーチェック
    var $sch;
    var $prg;
    var $auth;        //NO001
    var $moneycd;
    var $money;
    var $bedtime;
    var $risingtime;
    var $tvcd;
    var $tvprg;
    var $pc_hours;

    function knjh010Model()
    {
        unset($this->auth);
    }

    function init()
    {
        //プログラム起動時に変数初期化
        if(VARS::get("PROGRAMID")){
            unset($this->schregno);
            unset($this->name_show);
            unset($this->auth);    //NO001
        }
        //年組コンボボックス変更時に変数初期化
        if(VARS::get("init") == 1) {
            unset($this->schregno);
            unset($this->name_show);
        }
        //KNJH160からのコール
        if(VARS::get("PRG")) {
            $this->prg = VARS::get("PRG");
        }
        //NO001
        if(VARS::get("AUTH")) {
            $this->auth = VARS::get("AUTH");
        }
        $this->cmd        = VARS::request("cmd");
        $this->year     = CTRL_YEAR;
        if (VARS::get("SCHREGNO")){
            $this->schregno    = VARS::get("SCHREGNO");
        }
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if(VARS::get("NAME")){
            $this->name_show = VARS::get("NAME");
        }
        $this->field = array("DISEASE"            =>  VARS::post("DISEASE"),            //既往の患者
                             "HEALTHCONDITION"    =>  VARS::post("HEALTHCONDITION"),    //現在の健康状態
                             "MERITS"             =>  VARS::post("MERITS"),             //性質 長所
                             "DEMERITS"           =>  VARS::post("DEMERITS"),           //性質 短所
                             "GOOD_SUBJECT"       =>  VARS::post("GOOD_SUBJECT"),       //学業 得意科目
                             "BAD_SUBJECT"        =>  VARS::post("BAD_SUBJECT"),        //学業 不得意科目
                             "HOBBY"              =>  VARS::post("HOBBY"),              //趣味・娯楽
                             "OLD_CRAM"           =>  VARS::post("OLD_CRAM"),           //入学以前通っていた塾
                             "CUR_CRAM"           =>  VARS::post("CUR_CRAM"),           //塾名
                             "CUR_CRAMCD"         =>  VARS::post("CUR_CRAMCD"),         //現在通っている塾コンボ
                             "LESSONCD"           =>  VARS::post("LESSONCD"),           //けいこごとコンボ
                             "LESSON"             =>  VARS::post("LESSON"),             //けいこごと
                             "POCKETMONEYCD"      =>  VARS::post("POCKETMONEYCD"),      //こづかいコンボ
                             "POCKETMONEY"        =>  VARS::post("POCKETMONEY"),        //こづかい
                             "PRIZES"             =>  VARS::post("PRIZES"),             //賞罰・検定・その他
                             "BRO_SISCD"          =>  VARS::post("BRO_SISCD"),          //兄弟姉妹調査コンボ
                             "READING"            =>  VARS::post("READING"),            //読書傾向
                             "SPORTS"             =>  VARS::post("SPORTS"),             //スポーツ
                             "FRIENDSHIP"         =>  VARS::post("FRIENDSHIP"),         //交友
                             "PLANUNIV"           =>  VARS::post("PLANUNIV"),           //卒業後の志望：進学
                             "PLANJOB"            =>  VARS::post("PLANJOB"),            //卒業後の志望：就職
                             "ED_ACT"             =>  VARS::post("ED_ACT"),             //特別教育活動
                             "HOWTOCOMMUTECD"     =>  VARS::post("HOWTOCOMMUTECD"),     //通学方法コンボ
                             "COMMUTE_HOURS"      =>  VARS::post("COMMUTE_HOURS"),      //通学：所要時間
                             "COMMUTE_MINUTES"    =>  VARS::post("COMMUTE_MINUTES"),    //通学：所要分
                             "OTHERHOWTOCOMMUTE"  =>  VARS::post("OTHERHOWTOCOMMUTE"),  //通学 その他
                             "STATIONNAME"        =>  VARS::post("STATIONNAME"),        //通学 最寄駅名
                             "CHANGETRAIN1"       =>  VARS::post("CHANGETRAIN1"),       //通学 途中経由駅1
                             "CHANGETRAIN2"       =>  VARS::post("CHANGETRAIN2"),       //通学 途中経由駅2
                             "CHANGETRAIN3"       =>  VARS::post("CHANGETRAIN3"),       //通学 途中経由駅3
                             "CHANGETRAIN4"       =>  VARS::post("CHANGETRAIN4"),       //通学 途中経由駅4
                             "CHANGETRAIN5"       =>  VARS::post("CHANGETRAIN5"),       //通学 途中経由駅5
                             "CHANGETRAIN6"       =>  VARS::post("CHANGETRAIN6"),       //通学 途中経由駅6
                             "CHANGETRAIN7"       =>  VARS::post("CHANGETRAIN7"),       //通学 途中経由駅7
                             "CHANGETRAIN8"       =>  VARS::post("CHANGETRAIN8"),       //通学 途中経由駅8
                             "STUDYTIME"          =>  VARS::post("STUDYTIME"),          //学習時間コンボ
                             "BEDTIME_HOURS"      =>  VARS::post("BEDTIME_HOURS"),      //睡眠 就寝時間
                             "BEDTIME_MINUTES"    =>  VARS::post("BEDTIME_MINUTES"),    //睡眠 就寝分
                             "RISINGTIME_HOURS"   =>  VARS::post("RISINGTIME_HOURS"),   //睡眠 起床時間
                             "RISINGTIME_MINUTES" =>  VARS::post("RISINGTIME_MINUTES"), //睡眠 起床分
                             "TVVIEWINGHOURSCD"   =>  VARS::post("TVVIEWINGHOURSCD"),   //テレビの視聴時間コンボ
                             "TVPROGRAM"          =>  VARS::post("TVPROGRAM"),          //主に見るテレビ
                             "PC_HOURS"           =>  VARS::post("PC_HOURS"),           //パソコンの時間コンボ
                             "REMARK"             =>  VARS::post("REMARK"),             //備考
                             "RESIDENTCD"         =>  VARS::post("RESIDENTCD")          //住居調査コンボ
                            );
    }

    //入力項目チェック
    function validateInput()
    {
        //NO001
        if ($this->auth && $this->auth < DEF_UPDATE_RESTRICT){
            $this->setWarning("MSG300");
            return false;
        }
        if ($this->cmd == "update2"){
            if(strlen($this->field["POCKETMONEY"]) > 4){
                $this->setWarning("MSG915","こづかいは4バイトまでです。");
                return false;
            }
            elseif(strlen($this->field["TVPROGRAM"]) > 30){
                $this->setWarning("MSG915","主に見るテレビは30バイトまでです。");
                return false;
            }
        }else {
            if(strlen($this->field["DISEASE"]) > 30){
                $this->setWarning("MSG915","既往の疾患は30バイトまでです。");
                return false;
            }
            elseif(strlen($this->field["HEALTHCONDITION"]) > 30){
                $this->setWarning("MSG915","現在の健康状態は30バイトまでです。");
                return false;
            }
            elseif(strlen($this->field["MERITS"]) > 63){
                $this->setWarning("MSG915","長所は63バイトまでです。");
                return false;
            }
            elseif(strlen($this->field["DEMERITS"]) > 63){
                $this->setWarning("MSG915","短所は63バイトまでです。");
                return false;
            }
            elseif(strlen($this->field["GOOD_SUBJECT"]) > 63){
                $this->setWarning("MSG915","得意科目は63バイトまでです。");
                return false;
            }
            elseif(strlen($this->field["BAD_SUBJECT"]) > 63){
                $this->setWarning("MSG915","不得意科目は63バイトまでです。");
                return false;
            }
            elseif(strlen($this->field["HOBBY"]) > 63){
                $this->setWarning("MSG915","趣味娯楽は63バイトまでです。");
                return false;
            }
            elseif(strlen($this->field["OLD_CRAM"]) > 63){
                $this->setWarning("MSG915","入学以前通っていた塾は63バイトまでです。");
                return false;
            }
            elseif(strlen($this->field["CUR_CRAM"]) > 30){
                $this->setWarning("MSG915","現在通っている塾名は30バイトまでです。");
                return false;
            }
            elseif(strlen($this->field["LESSON"]) > 30){
                $this->setWarning("MSG915","けいこごとは30バイトまでです。");
                return false;
            }
            elseif(strlen($this->field["PRIZES"]) > 129){
                $this->setWarning("MSG915","賞罰・検定・その他は129バイトまでです。");
                return false;
            }
            elseif(strlen($this->field["READING"]) > 63){
                $this->setWarning("MSG915","読書傾向は63バイトまでです。");
                return false;
            }
            elseif(strlen($this->field["SPORTS"]) > 63){
                $this->setWarning("MSG915","スポーツは63バイトまでです。");
                return false;
            }
            elseif(strlen($this->field["FRIENDSHIP"]) > 63){
                $this->setWarning("MSG915","交友は63バイトまでです。");
                return false;
            }
            elseif(strlen($this->field["PLANUNIV"]) > 63){
                $this->setWarning("MSG915","進学は63バイトまでです。");
                return false;
            }
            elseif(strlen($this->field["PLANJOB"]) > 63){
                $this->setWarning("MSG915","就職は63バイトまでです。");
                return false;
            }
            elseif(strlen($this->field["ED_ACT"]) > 63){
                $this->setWarning("MSG915","特別教育活動は63バイトまでです。");
                return false;
            }
            elseif(strlen($this->field["COMMUTE_HOURS"]) > 2){
                $this->setWarning("MSG915","通学所要時間は2バイトまでです。");
                return false;
            }
            elseif(strlen($this->field["COMMUTE_MINUTES"]) > 2){
                $this->setWarning("MSG915","通学所要分は2バイトまでです。");
                return false;
            }
            elseif(strlen($this->field["OTHERHOWTOCOMMUTE"]) > 30){
                $this->setWarning("MSG915","最寄駅路線名は30バイトまでです。");
                return false;
            }
            elseif(strlen($this->field["STATIONNAME"]) > 30){
                $this->setWarning("MSG915","最寄駅名は30バイトまでです。");
                return false;
            }
            elseif(strlen($this->field["REMARK"]) > 129){
                $this->setWarning("MSG915","備考は129バイトまでです。");
                return false;
            }
        }
        //生徒が選択されていないときエラー表示
        if($this->cmd =="update" && $this->schregno==""){
            $this->setWarning("MSG308");
            return false;
        }
        return true;
    }

    //更新処理
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
        //年度及び学籍番号でSCHREG_ENVIR_DATを一旦削除(DELETE)し、追加(INSERT)
        if ($this->cmd == "update2"){
            knjh010Query::getUpdateQuery($this,$this->field);
        }else {
            knjh010Query::getDeleteQuery($this);
            knjh010Query::getInsertQuery($this,$this->field);
        }
        return true;
    }

    //削除処理
    function &getDeleteModel()
    {
        //NO001
        if ($this->auth && $this->auth < DEF_UPDATE_RESTRICT){
            $this->setWarning("MSG300");
            return false;
        }
        if ($this->schregno=="") {
            $this->setWarning("MSG308");
            return false;
        }
        //年度及び学籍番号でSCHREG_ENVIR_DATが存在する場合、削除(DELETE)
        $row = knjh010Query::getSchreg_envir_dat($this);        //SCHREG_ENVIR_DAT取得
        //データが存在しない場合エラー
        if($row == ""){
            $this->setWarning("MSG303");
            return false;
        }
        if($this->delete()){
            $this->setMessage("MSG201");
            return;
        }
    }
    function &delete()
    {
        knjh010Query::getDeleteQuery($this);
        return true;
    }
}
?>

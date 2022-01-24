<?php

require_once('for_php7.php');
class knjh010aModel extends Model
{
    public $field = array();
    public $cmd;
    public $schregno;    //学籍番号
    public $name_show;   //生徒の名前
    public $year;        //処理年度
    public $sc;          //セキュリティーチェック
    public $sch;
    public $prg;
    public $auth;
    public $moneycd;
    public $money;
    public $bedtime;
    public $risingtime;
    public $tvcd;
    public $tvprg;
    public $pc_hours;

    public function knjh010aModel()
    {
        unset($this->auth);
    }

    public function init()
    {
        //プログラム起動時に変数初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->schregno);
            unset($this->name_show);
            unset($this->auth);
        }
        //年組コンボボックス変更時に変数初期化
        if (VARS::get("init") == 1) {
            unset($this->schregno);
            unset($this->name_show);
        }
        //KNJH160Aからのコール
        if (VARS::get("PRG")) {
            $this->prg = VARS::get("PRG");
        }
        //権限
        if (VARS::get("AUTH")) {
            $this->auth = VARS::get("AUTH");
        }
        $this->cmd  = VARS::request("cmd");

        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->year = CTRL_YEAR;
        if (VARS::get("SCHREGNO")) {
            $this->schregno    = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME")) {
            $this->name_show = VARS::get("NAME");
        }
        $this->field = array("OLD_CRAM"           =>  VARS::post("OLD_CRAM"),           //入学以前通っていた塾
                             "CUR_CRAMCD"         =>  VARS::post("CUR_CRAMCD"),         //現在通っている塾コンボ
                             "CUR_CRAM"           =>  VARS::post("CUR_CRAM"),           //現在通っている塾名
                             "STUDYTIME"          =>  VARS::post("STUDYTIME"),          //学習時間コンボ
                             "LESSONCD"           =>  VARS::post("LESSONCD"),           //けいこごとコンボ
                             "LESSON"             =>  VARS::post("LESSON"),             //けいこごと
                             "PRIZES"             =>  VARS::post("PRIZES"),             //賞罰・検定・その他
                             "BRO_SISCD"          =>  VARS::post("BRO_SISCD"),          //兄弟姉妹調査コンボ
                             "RESIDENTCD"         =>  VARS::post("RESIDENTCD"),         //住居調査コンボ
                             "REMARK1"            =>  VARS::post("REMARK1"),            //住居調査(REMARK1)
                             "REMARK2"            =>  VARS::post("REMARK2"),            //住居調査(REMARK2)
                             "SPORTS"             =>  VARS::post("SPORTS"),             //スポーツ
                             "FRIENDSHIP"         =>  VARS::post("FRIENDSHIP"),         //交友
                             "PLANUNIV"           =>  VARS::post("PLANUNIV"),           //卒業後の志望：進学
                             "PLANJOB"            =>  VARS::post("PLANJOB"),            //卒業後の志望：就職
                             "ED_ACT"             =>  VARS::post("ED_ACT"),             //特別教育活動
                             "REMARK"             =>  VARS::post("REMARK"),             //備考

                             "GO_HOME_GROUP_NO"   =>  VARS::post("GO_HOME_GROUP_NO"),   //災害時帰宅グループ番号
                             "RESPONSIBILITY"     =>  VARS::post("RESPONSIBILITY"),     //責任者
                             "COMMUTE_HOURS"      =>  VARS::post("COMMUTE_HOURS"),      //通学：所要時間(時)
                             "COMMUTE_MINUTES"    =>  VARS::post("COMMUTE_MINUTES"),    //通学：所要時間(分)
                             "HOWTOCOMMUTECD"     =>  VARS::post("HOWTOCOMMUTECD"),     //通学方法コンボ
                             "UP_DOWN"            =>  VARS::post("UP_DOWN"),            //上り下り
                             "JOSYA_1"            =>  VARS::post("JOSYA_1"),            //通学 乗車駅1
                             "ROSEN_1"            =>  VARS::post("ROSEN_1"),            //通学 路線1
                             "GESYA_1"            =>  VARS::post("GESYA_1"),            //通学 下車駅1
                             "HIDDEN_JOSYA_1"     =>  VARS::post("HIDDEN_JOSYA_1"),     //通学 乗車駅1のコード
                             "HIDDEN_ROSEN_1"     =>  VARS::post("HIDDEN_ROSEN_1"),     //通学 路線1のコード
                             "HIDDEN_GESYA_1"     =>  VARS::post("HIDDEN_GESYA_1"),     //通学 下車駅1のコード
                             "FLG_1"              =>  VARS::post("FLG_1"),              //通学手段1

                             "JOSYA_2"            =>  VARS::post("JOSYA_2"),            //通学 乗車駅2
                             "ROSEN_2"            =>  VARS::post("ROSEN_2"),            //通学 路線2
                             "GESYA_2"            =>  VARS::post("GESYA_2"),            //通学 下車駅2
                             "HIDDEN_JOSYA_2"     =>  VARS::post("HIDDEN_JOSYA_2"),     //通学 乗車駅2のコード
                             "HIDDEN_ROSEN_2"     =>  VARS::post("HIDDEN_ROSEN_2"),     //通学 路線2のコード
                             "HIDDEN_GESYA_2"     =>  VARS::post("HIDDEN_GESYA_2"),     //通学 下車駅2のコード
                             "FLG_2"              =>  VARS::post("FLG_2"),              //通学手段2

                             "JOSYA_3"            =>  VARS::post("JOSYA_3"),            //通学 乗車駅3
                             "ROSEN_3"            =>  VARS::post("ROSEN_3"),            //通学 路線3
                             "GESYA_3"            =>  VARS::post("GESYA_3"),            //通学 下車駅3
                             "HIDDEN_JOSYA_3"     =>  VARS::post("HIDDEN_JOSYA_3"),     //通学 乗車駅3のコード
                             "HIDDEN_ROSEN_3"     =>  VARS::post("HIDDEN_ROSEN_3"),     //通学 路線3のコード
                             "HIDDEN_GESYA_3"     =>  VARS::post("HIDDEN_GESYA_3"),     //通学 下車駅3のコード
                             "FLG_3"              =>  VARS::post("FLG_3"),              //通学手段3

                             "JOSYA_4"            =>  VARS::post("JOSYA_4"),            //通学 乗車駅4
                             "ROSEN_4"            =>  VARS::post("ROSEN_4"),            //通学 路線4
                             "GESYA_4"            =>  VARS::post("GESYA_4"),            //通学 下車駅4
                             "HIDDEN_JOSYA_4"     =>  VARS::post("HIDDEN_JOSYA_4"),     //通学 乗車駅4のコード
                             "HIDDEN_ROSEN_4"     =>  VARS::post("HIDDEN_ROSEN_4"),     //通学 路線4のコード
                             "HIDDEN_GESYA_4"     =>  VARS::post("HIDDEN_GESYA_4"),     //通学 下車駅4のコード
                             "FLG_4"              =>  VARS::post("FLG_4"),              //通学手段4

                             "JOSYA_5"            =>  VARS::post("JOSYA_5"),            //通学 乗車駅5
                             "ROSEN_5"            =>  VARS::post("ROSEN_5"),            //通学 路線5
                             "GESYA_5"            =>  VARS::post("GESYA_5"),            //通学 下車駅5
                             "HIDDEN_JOSYA_5"     =>  VARS::post("HIDDEN_JOSYA_5"),     //通学 乗車駅5のコード
                             "HIDDEN_ROSEN_5"     =>  VARS::post("HIDDEN_ROSEN_5"),     //通学 路線5のコード
                             "HIDDEN_GESYA_5"     =>  VARS::post("HIDDEN_GESYA_5"),     //通学 下車駅5のコード
                             "FLG_5"              =>  VARS::post("FLG_5"),              //通学手段5

                             "JOSYA_6"            =>  VARS::post("JOSYA_6"),            //通学 乗車駅6
                             "ROSEN_6"            =>  VARS::post("ROSEN_6"),            //通学 路線6
                             "GESYA_6"            =>  VARS::post("GESYA_6"),            //通学 下車駅6
                             "HIDDEN_JOSYA_6"     =>  VARS::post("HIDDEN_JOSYA_6"),     //通学 乗車駅6のコード
                             "HIDDEN_ROSEN_6"     =>  VARS::post("HIDDEN_ROSEN_6"),     //通学 路線6のコード
                             "HIDDEN_GESYA_6"     =>  VARS::post("HIDDEN_GESYA_6"),     //通学 下車駅6のコード
                             "FLG_6"              =>  VARS::post("FLG_6"),              //通学手段6

                             "JOSYA_7"            =>  VARS::post("JOSYA_7"),            //通学 乗車駅7
                             "ROSEN_7"            =>  VARS::post("ROSEN_7"),            //通学 路線7
                             "GESYA_7"            =>  VARS::post("GESYA_7"),            //通学 下車駅7
                             "HIDDEN_JOSYA_7"     =>  VARS::post("HIDDEN_JOSYA_7"),     //通学 乗車駅7のコード
                             "HIDDEN_ROSEN_7"     =>  VARS::post("HIDDEN_ROSEN_7"),     //通学 路線7のコード
                             "HIDDEN_GESYA_7"     =>  VARS::post("HIDDEN_GESYA_7"),     //通学 下車駅7のコード
                             "FLG_7"              =>  VARS::post("FLG_7"),              //通学手段7

                             //subForm1からのデータ
                             "READING"            =>  VARS::post("READING"),            //読書傾向
                             "MERITS"             =>  VARS::post("MERITS"),             //性質 長所
                             "DEMERITS"           =>  VARS::post("DEMERITS"),           //性質 短所
                             "GOOD_SUBJECT"       =>  VARS::post("GOOD_SUBJECT"),       //学業 得意科目
                             "BAD_SUBJECT"        =>  VARS::post("BAD_SUBJECT"),        //学業 不得意科目
                             "HOBBY"              =>  VARS::post("HOBBY"),              //趣味・娯楽
                             "ATTENTIONMATTERS"   =>  VARS::post("ATTENTIONMATTERS"),   //要注意事項
                             "DISEASE"            =>  VARS::post("DISEASE"),            //既往の疾患
                             "HEALTHCONDITION"    =>  VARS::post("HEALTHCONDITION"),    //現在の健康状態
                             "POCKETMONEYCD"      =>  VARS::post("POCKETMONEYCD"),      //こづかい(コンボ)
                             "POCKETMONEY"        =>  VARS::post("POCKETMONEY"),        //こづかい(額)
                             "BEDTIME_HOURS"      =>  VARS::post("BEDTIME_HOURS"),      //睡眠 就寝時
                             "BEDTIME_MINUTES"    =>  VARS::post("BEDTIME_MINUTES"),    //睡眠 就寝分
                             "RISINGTIME_HOURS"   =>  VARS::post("RISINGTIME_HOURS"),   //睡眠 起床時間
                             "RISINGTIME_MINUTES" =>  VARS::post("RISINGTIME_MINUTES"), //睡眠 起床分
                             "TVVIEWINGHOURSCD"   =>  VARS::post("TVVIEWINGHOURSCD"),   //テレビの視聴時間コンボ
                             "TVPROGRAM"          =>  VARS::post("TVPROGRAM"),          //主に見るテレビ
                             "PC_HOURS"           =>  VARS::post("PC_HOURS"),           //パソコンの時間コンボ

                             //subForm2からのデータ
                             "AREA_SENTAKU"       =>  VARS::request("AREA_SENTAKU"),    //エリア選択コンボ
                             "ROSEN_SELECT"       =>  VARS::request("ROSEN_SELECT")     //路線選択コンボ
                            );
    }

    //入力項目チェック
    public function validateInput()
    {
        if ($this->auth && $this->auth < DEF_UPDATE_RESTRICT) {
            $this->setWarning(MSG300);
            return false;
        }
        if ($this->cmd == "update2") {
            if (strlen($this->field["POCKETMONEY"]) > 4) {
                $this->setWarning("MSG915", "こづかいは4バイトまでです。");
                return false;
            } elseif (strlen($this->field["TVPROGRAM"]) > 30) {
                $this->setWarning("MSG915", "主に見るテレビは30バイトまでです。");
                return false;
            } elseif (strlen($this->field["ATTENTIONMATTERS"]) > 30) {
                $this->setWarning("MSG915", "要注意事項は30バイトまでです。");
                return false;
            } elseif (strlen($this->field["DISEASE"]) > 30) {
                $this->setWarning("MSG915", "既往の疾患は30バイトまでです。");
                return false;
            } elseif (strlen($this->field["HEALTHCONDITION"]) > 30) {
                $this->setWarning("MSG915", "現在の健康状態は30バイトまでです。");
                return false;
            } elseif (strlen($this->field["MERITS"]) > 63) {
                $this->setWarning("MSG915", "長所は63バイトまでです。");
                return false;
            } elseif (strlen($this->field["DEMERITS"]) > 63) {
                $this->setWarning("MSG915", "短所は63バイトまでです。");
                return false;
            } elseif (strlen($this->field["GOOD_SUBJECT"]) > 63) {
                $this->setWarning("MSG915", "得意科目は63バイトまでです。");
                return false;
            } elseif (strlen($this->field["BAD_SUBJECT"]) > 63) {
                $this->setWarning("MSG915", "不得意科目は63バイトまでです。");
                return false;
            } elseif (strlen($this->field["HOBBY"]) > 63) {
                $this->setWarning("MSG915", "趣味娯楽は63バイトまでです。");
                return false;
            } elseif (strlen($this->field["READING"]) > 63) {
                $this->setWarning("MSG915", "読書傾向は63バイトまでです。");
                return false;
            }
        } else {
            if (strlen($this->field["OLD_CRAM"]) > 63) {
                $this->setWarning("MSG915", "入学以前通っていた塾は63バイトまでです。");
                return false;
            } elseif (strlen($this->field["CUR_CRAM"]) > 30) {
                $this->setWarning("MSG915", "現在通っている塾名は30バイトまでです。");
                return false;
            } elseif (strlen($this->field["LESSON"]) > 30) {
                $this->setWarning("MSG915", "けいこごとは30バイトまでです。");
                return false;
            } elseif (strlen($this->field["PRIZES"]) > 129) {
                $this->setWarning("MSG915", "賞罰・検定・その他は129バイトまでです。");
                return false;
            } elseif (strlen($this->field["SPORTS"]) > 63) {
                $this->setWarning("MSG915", "スポーツは63バイトまでです。");
                return false;
            } elseif (strlen($this->field["FRIENDSHIP"]) > 63) {
                $this->setWarning("MSG915", "交友は63バイトまでです。");
                return false;
            } elseif (strlen($this->field["PLANUNIV"]) > 63) {
                $this->setWarning("MSG915", "進学は63バイトまでです。");
                return false;
            } elseif (strlen($this->field["PLANJOB"]) > 63) {
                $this->setWarning("MSG915", "就職は63バイトまでです。");
                return false;
            } elseif (strlen($this->field["ED_ACT"]) > 63) {
                $this->setWarning("MSG915", "特別教育活動は63バイトまでです。");
                return false;
            } elseif (strlen($this->field["REMARK"]) > 129) {
                $this->setWarning("MSG915", "備考は129バイトまでです。");
                return false;
            } elseif (strlen($this->field["COMMUTE_HOURS"]) > 2) {
                $this->setWarning("MSG915", "通学所要時間は2バイトまでです。");
                return false;
            } elseif (strlen($this->field["COMMUTE_MINUTES"]) > 2) {
                $this->setWarning("MSG915", "通学所要分は2バイトまでです。");
                return false;
            } elseif (strlen($this->field["REMARK1"]) > 60) {
                $this->setWarning("MSG915", "住居調査２段目は60バイトまでです。");
                return false;
            } elseif (strlen($this->field["REMARK2"]) > 5) {
                $this->setWarning("MSG915", "住居調査３段目は5バイトまでです。");
                return false;
            }
        }
        //生徒が選択されていないときエラー表示
        if ($this->cmd =="update" && $this->schregno=="") {
            $this->setWarning("MSG308");
            return false;
        }
        return true;
    }

    //更新処理
    public function &getUpdateModel()
    {
        if (!$this->validateInput()) {
            return;
        }
        $this->update();
        $this->setMessage("MSG201");
    }
    public function &update()
    {
        //それぞれSCHREG_ENVIR_DATがあればアップデート、なければインサート
        if ($this->cmd == "update2") { //性質・学業・他の更新
            knjh010aQuery::getUpdateQuery($this, $this->field); //メソッド名がややこしいけど、アップデートまたはインサート
        } else { //メイン画面の更新
            knjh010aQuery::getInsertQuery($this, $this->field); //メソッド名がややこしいけど、アップデートまたはインサート
        }
        return true;
    }

    //削除処理
    public function &getDeleteModel()
    {
        if ($this->auth && $this->auth < DEF_UPDATE_RESTRICT) {
            $this->setWarning(MSG300);
            return false;
        }
        if ($this->schregno=="") {
            $this->setWarning("MSG308");
            return false;
        }
        //年度及び学籍番号でSCHREG_ENVIR_DATが存在する場合、削除(DELETE)
        $row = knjh010aQuery::getSchregEnvirDat($this);        //SCHREG_ENVIR_DAT取得
        //データが存在しない場合エラー
        if ($row == "") {
            $this->setWarning("MSG303");
            return false;
        }
        if ($this->delete()) {
            $this->setMessage("MSG201");
            return;
        }
    }
    public function &delete()
    {
        knjh010aQuery::getDeleteQuery($this);
        return true;
    }
}

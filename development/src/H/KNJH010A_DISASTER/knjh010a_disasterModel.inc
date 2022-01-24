<?php

require_once('for_php7.php');

class knjh010a_disasterModel extends Model
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

    public function knjh010a_disasterModel()
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
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->cmd  = VARS::request("cmd");
        $this->year = CTRL_YEAR;
        if (VARS::get("SCHREGNO")) {
            $this->schregno    = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME")) {
            $this->name_show = VARS::get("NAME");
        }
        $this->field = array("RESIDENTCD"         =>  VARS::post("RESIDENTCD"),         //住居調査コンボ
                             "REMARK1"            =>  VARS::post("REMARK1"),            //住居調査(REMARK1)
                             "REMARK2"            =>  VARS::post("REMARK2"),            //住居調査(REMARK2)
                             "GO_HOME_GROUP_NO"   =>  VARS::post("GO_HOME_GROUP_NO"),   //災害時帰宅グループ番号
                             "RESPONSIBILITY"     =>  VARS::post("RESPONSIBILITY"),     //責任者
                             "HIKINAME1"          =>  VARS::post("HIKINAME1"),          //引き取り者氏名１
                             "RELATION1"          =>  VARS::post("RELATION1"),          //本人との関係１
                             "HIKITELNO1"         =>  VARS::post("HIKITELNO1"),         //電話番号１
                             "HIKINAME2"          =>  VARS::post("HIKINAME2"),          //引き取り者氏名２
                             "RELATION2"          =>  VARS::post("RELATION2"),          //本人との関係２
                             "HIKITELNO2"         =>  VARS::post("HIKITELNO2"),         //電話番号２
                             "HIKINAME3"          =>  VARS::post("HIKINAME3"),          //引き取り者氏名３
                             "RELATION3"          =>  VARS::post("RELATION3"),          //本人との関係３
                             "HIKITELNO3"         =>  VARS::post("HIKITELNO3"),         //電話番号３
                             /*避難先１*/
                             "HINANAMES"          =>  VARS::post("HINANAMES"),          //避難先名
                             "HINAZIPS"           =>  VARS::post("HINAZIPS"),           //郵便番号
                             "HINAADD1S"          =>  VARS::post("HINAADD1S"),          //住所
                             "HINAADD2S"          =>  VARS::post("HINAADD2S"),          //方書き
                             "HINATEL1S"          =>  VARS::post("HINATEL1S"),          //電話番号１
                             "HINATEL2S"          =>  VARS::post("HINATEL2S"),          //電話番号２
                             /*避難先２*/
                             "HINANAMEH"          =>  VARS::post("HINANAMEH"),          //避難先名
                             "HINAZIPH"           =>  VARS::post("HINAZIPH"),           //郵便番号
                             "HINAADD1H"          =>  VARS::post("HINAADD1H"),          //住所
                             "HINAADD2H"          =>  VARS::post("HINAADD2H"),          //方書き
                             "HINATEL1H"          =>  VARS::post("HINATEL1H"),          //電話番号１
                             "HINATEL2H"          =>  VARS::post("HINATEL2H"),          //電話番号２
                             /*備考*/
                             "BIKONAME1"          =>  VARS::post("BIKONAME1"),          //氏名１
                             "BIKOGROUP1"         =>  VARS::post("BIKOGROUP1"),         //所属１
                             "BIKONAME2"          =>  VARS::post("BIKONAME2"),          //氏名２
                             "BIKOGROUP2"         =>  VARS::post("BIKOGROUP2"),         //所属２

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

                             //subForm2からのデータ
                             "AREA_SENTAKU"       =>  VARS::request("AREA_SENTAKU"),    //エリア選択コンボ
                             "ROSEN_SELECT"       =>  VARS::request("ROSEN_SELECT")     //路線選択コンボ
                            );
    }

    //入力項目チェック
    public function validateInput()
    {
        if ($this->auth && $this->auth < DEF_UPDATE_RESTRICT) {
            $this->setWarning("MSG300");
            return false;
        }
        if (strlen($this->field["REMARK1"]) > 60) {
            $this->setWarning("MSG915", "住居調査２段目は60バイトまでです。");
            return false;
        } elseif (strlen($this->field["REMARK2"]) > 5) {
            $this->setWarning("MSG915", "住居調査３段目は5バイトまでです。");
            return false;
        } elseif (strlen($this->field["HIKINAME1"]) > 120) {
            $this->setWarning("MSG915", "引き取り者氏名１は120バイトまでです。");
            return false;
        } elseif (strlen($this->field["HIKITELNO1"]) > 14) {
            $this->setWarning("MSG915", "電話番号１は14バイトまでです。");
            return false;
        } elseif (strlen($this->field["HIKINAME2"]) > 120) {
            $this->setWarning("MSG915", "引き取り者氏名２は120バイトまでです。");
            return false;
        } elseif (strlen($this->field["HIKITELNO2"]) > 14) {
            $this->setWarning("MSG915", "電話番号２は14バイトまでです。");
            return false;
        } elseif (strlen($this->field["HIKINAME3"]) > 120) {
            $this->setWarning("MSG915", "引き取り者氏名３は120バイトまでです。");
            return false;
        } elseif (strlen($this->field["HIKITELNO3"]) > 14) {
            $this->setWarning("MSG915", "電話番号３は14バイトまでです。");
            return false;
        } elseif (strlen($this->field["HINANAMES"]) > 150) {
            $this->setWarning("MSG915", "避難先１の避難先名は150バイトまでです。");
            return false;
        } elseif (strlen($this->field["HINAZIPS"]) > 8) {
            $this->setWarning("MSG915", "避難先１の郵便番号は8バイトまでです。");
            return false;
        } elseif (strlen($this->field["HINAADD1S"]) > 150) {
            $this->setWarning("MSG915", "避難先１の住所は150バイトまでです。");
            return false;
        } elseif (strlen($this->field["HINAADD2S"]) > 150) {
            $this->setWarning("MSG915", "避難先１の方書きは150バイトまでです。");
            return false;
        } elseif (strlen($this->field["HINATEL1S"]) > 14) {
            $this->setWarning("MSG915", "避難先１の電話番号１は14バイトまでです。");
            return false;
        } elseif (strlen($this->field["HINATEL2S"]) > 14) {
            $this->setWarning("MSG915", "避難先１の電話番号２は14バイトまでです。");
            return false;
        } elseif (strlen($this->field["HINANAMEH"]) > 150) {
            $this->setWarning("MSG915", "避難先２の避難先名は150バイトまでです。");
            return false;
        } elseif (strlen($this->field["HINAZIPH"]) > 8) {
            $this->setWarning("MSG915", "避難先２の郵便番号は8バイトまでです。");
            return false;
        } elseif (strlen($this->field["HINAADD1H"]) > 150) {
            $this->setWarning("MSG915", "避難先２の住所は150バイトまでです。");
            return false;
        } elseif (strlen($this->field["HINAADD2H"]) > 150) {
            $this->setWarning("MSG915", "避難先２の方書きは150バイトまでです。");
            return false;
        } elseif (strlen($this->field["HINATEL1H"]) > 14) {
            $this->setWarning("MSG915", "避難先２の電話番号１は14バイトまでです。");
            return false;
        } elseif (strlen($this->field["HINATEL2H"]) > 14) {
            $this->setWarning("MSG915", "避難先２の電話番号２は14バイトまでです。");
            return false;
        } elseif (strlen($this->field["BIKONAME1"]) > 120) {
            $this->setWarning("MSG915", "備考氏名１は120バイトまでです。");
            return false;
        } elseif (strlen($this->field["BIKOGROUP1"]) > 150) {
            $this->setWarning("MSG915", "備考所属１は150バイトまでです。");
            return false;
        } elseif (strlen($this->field["BIKONAME2"]) > 120) {
            $this->setWarning("MSG915", "備考氏名２は120バイトまでです。");
            return false;
        } elseif (strlen($this->field["BIKOGROUP2"]) > 150) {
            $this->setWarning("MSG915", "備考所属２は150バイトまでです。");
            return false;
        } elseif (strlen($this->field["COMMUTE_HOURS"]) > 2) {
            $this->setWarning("MSG915", "通学所要時間は2バイトまでです。");
            return false;
        } elseif (strlen($this->field["COMMUTE_MINUTES"]) > 2) {
            $this->setWarning("MSG915", "通学所要分は2バイトまでです。");
            return false;
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
        //それぞれSCHREG_ENVIR_DAT、SCHREG_ENVIR_DETAIL_DATがあればアップデート、なければインサート
        knjh010a_disasterQuery::getInsertQuery($this, $this->field, $db); //メソッド名がややこしいけど、アップデートまたはインサート
        knjh010a_disasterQuery::getUpdateDetailQuery($this, $this->field, $db);
        return true;
    }

    //削除処理
    public function &getDeleteModel()
    {
        if ($this->auth && $this->auth < DEF_UPDATE_RESTRICT) {
            $this->setWarning("MSG300");
            return false;
        }
        if ($this->schregno=="") {
            $this->setWarning("MSG308");
            return false;
        }
        //年度及び学籍番号でSCHREG_ENVIR_DATが存在する場合、削除(DELETE)
        $row  = knjh010a_disasterQuery::getSchregEnvirDat($this);        //SCHREG_ENVIR_DAT取得
        $row2 = knjh010a_disasterQuery::getSchregEnvirDetailDat($this);    //SCHREG_ENVIR_DETAIL_DAT取得
        //データが存在しない場合エラー
        if ($row == "" && $row2 == "") {
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
        knjh010a_disasterQuery::getDeleteQuery($this);
        return true;
    }
}

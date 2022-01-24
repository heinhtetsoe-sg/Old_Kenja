<?php

require_once('for_php7.php');


class knjm700Model extends Model {

    var $field  = array();      //画面データ格納
    var $delArray = array();    //削除データ配列
    var $schcntall;             //登録データ数
    var $cmd;                   //コマンド
    var $control;               //コントロールマスタ

    function knjm700Model()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->cmd  = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();

        if ($this->cmd != 'read' && $this->cmd != 'addread') {
            $this->field = array("YEAR"         =>  CTRL_YEAR,                  //年度
                                 "SEMESTER"     =>  CTRL_SEMESTER,              //学期
                                 "SCHREGNO"     =>  VARS::post("SCHREGNO"),     //学籍番号
                                 "CLASSCD"      =>  VARS::post("CLASSCD"),      //教科
                                 "SUBCLASSCD"   =>  VARS::post("SUBCLASSCD"),   //科目
                                 "CHAIRCD"      =>  VARS::post("CHAIRCD"),      //講座
                                 "PERIODF"      =>  VARS::post("PERIODF"),      //校時From
                                 "PERIODT"      =>  VARS::post("PERIODT"),      //校時To
                                 "CREDIT_TIME"  =>  VARS::post("CREDIT_TIME"),  //単位時間
                                 "ATTENDDATE"   =>  $this->cmd == "" ? str_replace("-", "/", CTRL_DATE) : VARS::post("ATTENDDATE"),   //出席日付
                                 "REMARK"       =>  VARS::post("REMARK")        //備考
                         );
        }

    }

    //入力項目チェック
    function validateInput()
    {
        if (VARS::request("cmd") == 'add') {

            $hissuCheck = array("SCHREGNO"     =>  "学籍番号",
                                "CLASSCD"      =>  "教科",
                                "SUBCLASSCD"   =>  "科目",
                                "CHAIRCD"      =>  "講座",
                                "PERIODF"      =>  "校時From",
                                "PERIODT"      =>  "校時To",
                                "CREDIT_TIME"  =>  "単位時間",
                                "ATTENDDATE"   =>  "出席日付"
                         );

            foreach ($hissuCheck as $hissuKey => $hissuVal) {
                if (!$this->field[$hissuKey]) {
                    $this->setWarning($hissuVal."を入力して下さい。");
                    return false;
                }
            }

            if (strlen($this->field["REMARK"]) > 90) {
                $this->setWarning("備考は90バイトまです。".strlen($this->field["REMARK"]));
                return false;
            }

            if (strlen($this->field["SCHREGNO"]) != 8) {
                $this->setWarning("学籍番号は8桁です。");
                return false;
            }

            if ($this->field["PERIODF"] > $this->field["PERIODT"]) {
                $this->setWarning("校時指定が不正です。");
                return false;
            }

            if ($this->control["学期開始日付"]["9"] > $this->field["ATTENDDATE"] || $this->control["学期終了日付"]["9"] < $this->field["ATTENDDATE"]) {
                $this->setWarning("MSG915","(年度範囲外)");
                return false;
            }

            if ($this->field["ATTENDDATE"] > str_replace("-", "/", CTRL_DATE)) {
                $this->setWarning("MSG915","(処理日付がオーバーしています)");
                return false;
            }

            $db = Query::dbCheckOut();

            $query = knjm700Query::getChairStdCnt($this);
            $chairStd = $db->getOne($query);

            if ($chairStd == 0) {
                $this->setWarning("指定講座の名簿に生徒が存在しません。");
                return false;
            }

            Query::dbCheckIn($db);
        }
        return true;
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            if (VARS::request("cmd") == 'add') {
                $this->insert();
            } else if (VARS::request("cmd") == 'alldel') {
                $this->delete();
            } else {
                $this->delArray = array();
                $delc = 0;
                for ($i = 0; $i < $this->schcntall; $i++) {
                    if (VARS::post("DELCHK".$i) == "on") {
                        list($year, $semester, $schregno, $subclasscd, $attenddate, $periodf) = preg_split("/:/", VARS::post("DEL_KEY".$i));
                        $this->delArray[$delc] = array("YEAR"        => $year,
                                                       "SEMESTER"    => $semester,
                                                       "SCHREGNO"    => $schregno,
                                                       "SUBCLASSCD"  => $subclasscd,
                                                       "ATTENDDATE"  => $attenddate,
                                                       "PERIODF"     => $periodf);
                        $delc++;
                    }
                }
                foreach ($this->delArray as $delKey => $delVal) {
                    $this->deletesitei($delVal);
                }
                if (get_count($this->delArray) == 0) {
                    $this->setWarning("削除データを選択して下さい。");
                    return false;
                }
            }

        }
    }

    /**
     * インサート時の内部メソッド
     */
    function &insert()
    {
        $this->deletesitei($this->field);    //NO001
        knjm700Query::getInsertQuery($this);
    }
    /**
     * デリート時の内部メソッド
     */
    function &delete()
    {
        knjm700Query::getDeleteQuery($this);
    }
    function &deletesitei($data)
    {
        knjm700Query::getDeleteQuerysitei($data, $this);
    }

}
?>

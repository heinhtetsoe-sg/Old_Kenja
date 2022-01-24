<?php

require_once('for_php7.php');

class knji130Model extends Model
{
    public $field = array();
    public $cmd;
    public $schoolKind = "";
    public $schregno;
    public $control = array();         //コントロールマスタデータを保持
    public $exp_year;
    public $exp_semester;
    public $search = array();

    public function knji130Model()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->schregno);
            unset($this->search);
            unset($this->field);

            unset($this->grade);
            unset($this->exp_year);
            unset($this->exp_semester);
        }

        $this->cmd = VARS::request("cmd");

        //生徒情報
        $this->field = array("SCHREGNO"         =>  VARS::post("SCHREGNO"),         //学籍番号
                             "NAME"             =>  VARS::post("NAME"),             //氏名
                             "GRCL"             =>  VARS::post("GRCL"),             //年組
                             "ATTENDNO"         =>  VARS::post("ATTENDNO"),         //出席番号
                             "ANNUAL"           =>  VARS::post("ANNUAL"),           //年次
                             "COURSEMAJORCD"    =>  VARS::post("COURSEMAJORCD"),    //課程学科
                             "COURSECODE"       =>  VARS::post("COURSECODE"),       //コースコード
                             "INOUTCD"          =>  VARS::post("INOUTCD"),          //内外区分
                             "FUKUGAKU_DATE"    =>  VARS::post("ENT_DATE"),         //復学日＝入学日
                             "UPDATED1"         =>  VARS::post("UPDATED1"),         //更新日付
                             "UPDATED2"         =>  VARS::post("UPDATED2"),         //更新日付
                             "ENT_DATE"         =>  VARS::post("ENT_DATE"),         //入学日
                             "ENT_DIV"          =>  VARS::post("ENT_DIV"),          //入学区分
                             "ENT_REASON"       =>  VARS::post("ENT_REASON"),       //入学事由
                             "ENT_SCHOOL"       =>  VARS::post("ENT_SCHOOL"),       //入学学校
                             "CURRICULUM_YEAR"  =>  VARS::post("CURRICULUM_YEAR"),  //教育課程
                             "ENT_ADDR"         =>  VARS::post("ENT_ADDR"),         //入学住所
                             "ENT_ADDR2"        =>  VARS::post("ENT_ADDR2"),        //入学住所２
                            );

        if (VARS::get("SCHREGNO") != "") {
            $this->schregno     = VARS::get("SCHREGNO");
        }
        if (VARS::get("EXP_YEAR") != "") {
            $this->exp_year     = VARS::get("EXP_YEAR");
        }
        if (VARS::get("EXP_SEMESTER") != "") {
            $this->exp_semester     = VARS::get("EXP_SEMESTER");
        }
        if (VARS::get("GRADE") != "") {
            $this->grade     = VARS::get("GRADE");
        }

        //学校種別
        $this->schoolKind = knji130Query::getSchoolKind($this, $this->grade);

        if ($this->cmd == "search" || $this->cmd == "right") {
            //検索用
            $this->search = array("GRD_YEAR"        => VARS::post("GRD_YEAR"),
                                  "GRADE_HR_CLASS"  => VARS::post("GRADE_HR_CLASS"),
                                  "COURSECODE"      => VARS::post("COURSECODE"),
                                  "S_SCHREGNO"      => VARS::post("S_SCHREGNO"),
                                  "NAME"            => VARS::post("NAME"),
                                  "NAME_SHOW"       => VARS::post("NAME_SHOW"),
                                  "NAME_KANA"       => VARS::post("NAME_KANA"),
                                  "SEX"             => VARS::post("SEX"),
                                 );

            unset($this->field);
            unset($this->schregno);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        if ($this->schregno == "") {
            $this->setWarning("MSG308");
            return false;
        }

        //データ変更チェック
        $db = Query::dbCheckOut();
        $query = knji130Query::getStudentData($this);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        if ($row["UPDATED1"] != $this->field["UPDATED1"] || $row["UPDATED2"] != $this->field["UPDATED2"]) {
            $this->setWarning("MSG307");
            return false;
        }

        //必須項目チェック
        $flag = false;
        $msg ="";
        //年組
        if ($this->field["GRCL"] == "") {
            $flag = true;
            $msg .= "\\n年組";
        }
        //出席番号
        if ($this->field["ATTENDNO"] == "") {
            $flag = true;
            $msg .= "\\n出席番号";
        }
        //年次
        if ($this->field["ANNUAL"] == "") {
            $flag = true;
            $msg .= "\\n年次";
        }
        //課程学科コード
        if ($this->field["COURSEMAJORCD"] == "") {
            $flag = true;
            $msg .= "\\n課程学科";
        }
        //コースコード
        if ($this->field["COURSECODE"] == "") {
            $flag = true;
            $msg .= "\\nコース";
        }
        //内外区分
        if ($this->field["INOUTCD"] == "") {
            $flag = true;
            $msg .= "\\n内外区分";
        }
        //復学日
        if ($this->field["FUKUGAKU_DATE"] == "") {
            $flag = true;
            $msg .= "\\n入学日";
        }
        //入学区分
        if ($this->field["ENT_DIV"] == "") {
            $flag = true;
            $msg .= "\\n入学区分";
        }

        if ($flag == true) {
            $this->setWarning("MSG301", $msg);
            return false;
        }

        if ($this->field["ANNUAL"] == "0") {
            $this->setWarning("MSG916", "( 年次 ) 1～99まで");
            return false;
        }

        return true;
    }

    //データが存在するかチェック
    public function existCheck($table_flag)
    {
        if ($table_flag == "fukugaku_date") {
            return knji130Query::isExistComebackDate($this);
        }
        if ($table_flag == "attendno") {
            return knji130Query::isExistAttendno($this);
        }
    }

    //復学モデル
    public function &getUpdateModel()
    {
        if ($this->validateInput()) {
            if ($this->existCheck("fukugaku_date")) {
                $this->setWarning("MSG302", "( 復学日 )");
                return;
            }
            if ($this->existCheck("attendno")) {
                $this->setWarning("MSG302", "( 出席番号 )");
                return;
            }

            //復学
            $this->update();
            $this->setMessage("MSG201");

            //セット
            $this->exp_year     = CTRL_YEAR;
            $this->exp_semester = CTRL_SEMESTER;
        }

        return;
    }

    //復学処理
    public function &update()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //更新テーブル一覧
        //MOTO：コピー元　SAKI：コピー先　FIELDNO：COMEBACK_DATEの位置　FLG：1⇒MOTOテーブル削除対象
        $table[] = (array("MOTO" => "GRD_ADDRESS_DAT",           "SAKI" => "GRD_ADDRESS_COMEBACK_DAT",           "FIELDNO" => "2", "FLG" => "1"));
        $table[] = (array("MOTO" => "GRD_BASE_MST",              "SAKI" => "GRD_BASE_COMEBACK_MST",              "FIELDNO" => "1", "FLG" => "1"));
        $table[] = (array("MOTO" => "GRD_GUARANTOR_ADDRESS_DAT", "SAKI" => "GRD_GUARANTOR_ADDRESS_COMEBACK_DAT", "FIELDNO" => "2", "FLG" => "1"));
        $table[] = (array("MOTO" => "GRD_GUARDIAN_ADDRESS_DAT",  "SAKI" => "GRD_GUARDIAN_ADDRESS_COMEBACK_DAT",  "FIELDNO" => "2", "FLG" => "1"));
        $table[] = (array("MOTO" => "GRD_GUARDIAN_DAT",          "SAKI" => "GRD_GUARDIAN_COMEBACK_DAT",          "FIELDNO" => "1", "FLG" => "1"));
        $table[] = (array("MOTO" => "GRD_REGD_DAT",              "SAKI" => "GRD_REGD_COMEBACK_DAT",              "FIELDNO" => "3", "FLG" => "1"));
        $table[] = (array("MOTO" => "SCHREG_ENT_GRD_HIST_DAT",   "SAKI" => "SCHREG_ENT_GRD_HIST_COMEBACK_DAT",   "FIELDNO" => "2", "FLG" => "0"));

        //復学日
        $fukugaku_date = str_replace("/", "-", $this->field["FUKUGAKU_DATE"]);

        for ($i=0; $i<get_count($table); $i++) {
            $db->autoCommit(false);

            //バックアップテーブル削除
            $query = knji130Query::deleteTableQuery($this, $table[$i]["SAKI"], $fukugaku_date);
            $db->query($query);

            //更新対象フィールド一覧取得
            $Row = $db->getRow(knji130Query::getFieldQuery($this, $table[$i]["MOTO"]), DB_FETCHMODE_ASSOC);
            $setField = "";
            $field = "";
            $sep = "";
            $cnt = 0;

            if (is_array($Row)) {
                foreach ($Row as $key => $val) {
                    if ($cnt == $table[$i]["FIELDNO"]) {
                        $field .= $sep."'".$fukugaku_date."'";
                        $setField .= $sep."COMEBACK_DATE";
                    }

                    $setField .= $sep.$key;
                    if ($key == "REGISTERCD") {
                        $key = "'".STAFFCD."'";
                    } elseif ($key == "UPDATED") {
                        $key = "SYSDATE()";
                    }
                    $field .= $sep.$key;
                    $sep = ",";

                    $cnt++;
                }

                //バックアップテーブル追加
                $query = knji130Query::insertComeBackTable($db, $this, $table[$i]["SAKI"], $table[$i]["MOTO"], $field, $setField);
                $db->query($query);
            }

            if ($table[$i]["FLG"] == "1") {
                //卒業生テーブル削除
                $query = knji130Query::deleteTableQuery($this, $table[$i]["MOTO"]);
                $db->query($query);
            }

            $db->commit(); // トランザクションをコミットする。
        }

        //学校種別
        list($grade, $hr_class) = explode(",", $this->field["GRCL"]);
        $this->schoolKind = knji130Query::getSchoolKind($this, $grade);

        //DB切断
        Query::dbCheckIn($db);

        //学籍学歴データ更新
        knji130Query::getInsUpdEntGrd($this);
        //在籍データ更新
        knji130Query::getUpdateQuery($this, "SCHREG_REGD_DAT");
        //基礎マスタ更新
        knji130Query::getUpdateQuery($this, "SCHREG_BASE_MST");
        //基礎詳細マスタ更新
        knji130Query::getUpdateQuery($this, "SCHREG_BASE_DETAIL_MST");
    }

    //各コンボボックス作成
    public function createCombo($objForm, $db, $namecd, $varname, $value, $fst)
    {
        $result = $db->query(knji130Query::getNameMstData($namecd));
        $opt = array();

        //性別と卒業区分には先頭に空をセット
        if ($fst=="1") {
            $opt[] = array("label" => "","value" => "");
        }

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array( "label" => $row["NAMECD2"]."  ".htmlspecialchars($row["NAME1"]),
                              "value" => $row["NAMECD2"]);
        }

        $objForm->ae(array("type"        => "select",
                            "name"        => $varname,
                            "size"        => 1,
                            "maxlength"   => 10,
                            "extrahtml"   => "",
                            "value"       => $value,
                            "options"     => $opt));

        return $objForm->ge($varname);
    }
}

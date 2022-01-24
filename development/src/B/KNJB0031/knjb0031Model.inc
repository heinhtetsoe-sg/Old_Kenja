<?php

require_once('for_php7.php');

class knjb0031Model extends Model
{
    public $fields = array();
    public $cmd;
    public $term;
    public $group;
    public $grade_course;
    public $subclasscd;
    public $kensuu;
    public $data_cnt;
    public $control;
    public $z010name1;
    public $keikokutenFlag;
    public $lastColumn;//CSVの最後の列の値

    public function knjb0031Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        if (VARS::request("PROGRAMID") != "") {
            $this->term  = CTRL_YEAR. "-" .CTRL_SEMESTER;

            //学校を判断
            $db = Query::dbCheckOut();
            $this->z010name1 = $db->getOne(knjb0031Query::getNameMstZ010());
            Query::dbCheckIn($db);
        }

        if (VARS::request("term") != "") {
            $this->term  = VARS::request("term");
        }
        $this->grade_course = VARS::request("GRADE_COURSE");
        $this->subclasscd = VARS::request("SUBCLASSCD");
        $this->group = VARS::request("GROUP");

        $this->kensuu = VARS::post("KENSUU");
        $this->data_cnt = VARS::post("DATA_CNT");

        $this->cmd   = VARS::request("cmd");

        //初期化
        $this->fields = array();
        if ($this->cmd == 'update' || $this->cmd == 'delete' || $this->cmd == 'csv2') {
            for ($g = 0; $g < $this->data_cnt; $g++) {
                $this->fields["DEL_FLG"][]         =  VARS::post("DEL_FLG"."-".$g);

                $this->fields["CHAIRCD"][]             =  VARS::post("CHAIRCD"."-".$g);
                $this->fields["GROUPCD"][]             =  VARS::post("GROUPCD"."-".$g);
                $this->fields["CHAIRNAME"][]           =  VARS::post("CHAIRNAME"."-".$g);
                $this->fields["CHAIRABBV"][]           =  VARS::post("CHAIRABBV"."-".$g);
                $this->fields["TAKESEMES"][]           =  VARS::post("TAKESEMES"."-".$g);
                $this->fields["LESSONCNT"][]           =  VARS::post("LESSONCNT"."-".$g);
                $this->fields["FRAMECNT"][]            =  VARS::post("FRAMECNT"."-".$g);
                $this->fields["COUNTFLG"][]            =  VARS::post("COUNTFLG"."-".$g);
                $this->fields["STD_CNT"][]             =  VARS::post("STD_CNT"."-".$g);
                $this->fields["KEIKOKUTEN_SOTEN"][]    =  VARS::post("KEIKOKUTEN_SOTEN"."-".$g);
                $this->fields["KEIKOKUTEN_HYOUKA"][]   =  VARS::post("KEIKOKUTEN_HYOUKA"."-".$g);
                $this->fields["KEIKOKUTEN_HYOUTEI"][]  =  VARS::post("KEIKOKUTEN_HYOUTEI"."-".$g);
                $this->fields["SEQ004_REMARK1"][]      =  VARS::post("SEQ004_REMARK1"."-".$g);
                $this->fields["SEQ004_REMARK2"][]      =  VARS::post("SEQ004_REMARK2"."-".$g);
                $this->fields["SEQ004_REMARK3"][]      =  VARS::post("SEQ004_REMARK3"."-".$g);

                $this->fields["FACILITYABBV"][]        =  VARS::post("FACILITYABBV"."-".$g);
                $this->fields["FACCD"][]               =  VARS::post("FACCD"."-".$g);
                $this->fields["TEXTBOOKABBV"][]        =  VARS::post("TEXTBOOKABBV"."-".$g);
                $this->fields["TEXTBOOKCD"][]          =  VARS::post("TEXTBOOKCD"."-".$g);
                $this->fields["STAFFNAME_SHOW1"][]     =  VARS::post("STAFFNAME_SHOW1"."-".$g);
                $this->fields["STAFFNAME_SHOW"][]      =  VARS::post("STAFFNAME_SHOW"."-".$g);
                $this->fields["STAFFCD"][]             =  VARS::post("STAFFCD"."-".$g);
                $this->fields["CHARGEDIV"][]           =  VARS::post("CHARGEDIV"."-".$g);
                $this->fields["STF_CHARGE"][]          =  VARS::post("STF_CHARGE"."-".$g);
                $this->fields["HR_NAMEABBV"][]         =  VARS::post("HR_NAMEABBV"."-".$g);
                $this->fields["GRADE_CLASS"][]         =  VARS::post("GRADE_CLASS"."-".$g);
                $this->fields["UPDATED"][]             =  VARS::post("UPDATED"."-".$g);
            }//for
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $db = Query::dbCheckOut();
        $this->keikokutenFlag = ($db->getOne(knjb0031Query::getIsKeikokuten($this)) == '1' && $this->Properties["use_school_Detail_009"] == "1");
        Query::dbCheckIn($db);

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        for ($g = 0; $g < $this->data_cnt; $g++) {
            //キーの更新チェック
            if (!strlen($this->fields["CHAIRCD"][$g])) {
                $this->setWarning("MSG301", "(講座コード)");
                return false;
            }
            if (!strlen($this->subclasscd)) {
                $this->setWarning("MSG301", "(科目コード)");
                return false;
            }
            if (sprintf("%d", $this->fields["CHAIRCD"][$g]) < 1) {
                $this->warning = "１以上を入力して下さい。(講座コード)";
                return false;
            }
            //講座コードゼロ埋め
            if (strlen($this->fields["CHAIRCD"][$g]) < 7) {
                $this->fields["CHAIRCD"][$g] = sprintf("%07d", $this->fields["CHAIRCD"][$g]);
            }
            //文字数チェック
            if (strlen($this->fields["CHAIRCD"][$g]) > 7) {
                $this->setWarning("MSG915", "(講座コード) 7バイトまでです。");
                return false;
            } elseif (strlen($this->fields["CHAIRNAME"][$g]) > 30) {
                $this->setWarning("MSG915", "(講座名称) 30バイトまでです。");
                return false;
            } elseif (strlen($this->fields["CHAIRABBV"][$g]) > 15) {
                $this->setWarning("MSG915", "(講座略称) 15バイトまでです。");
                return false;
            } elseif (strlen($this->fields["STD_CNT"][$g]) > 3) {
                $this->setWarning("MSG915", "(受講人数) 3バイトまでです。");
                return false;
            } elseif (strlen($this->fields["SEQ004_REMARK2"][$g]) > 15) {
                $this->setWarning("MSG915", "(スモールクラス名称) 15バイトまでです。");
                return false;
            } elseif (strlen($this->fields["SEQ004_REMARK3"][$g]) > 15) {
                $this->setWarning("MSG915", "(習熟度クラス名称) 15バイトまでです。");
                return false;
            }
            //大小チェック（連続枠数＞週授業回数ならエラー）
            if ($this->fields["FRAMECNT"][$g] > $this->fields["LESSONCNT"][$g]) {
                $this->warning = "連続授業が授業回数を超えています。";
                return false;
            }
            /***
                        //リストよりデータを選択後、講座コードを変更した場合
                        if ($this->cmd == "update" && $this->chaircd != $this->fields["CHAIRCD"]) {
                            $this->setWarning("MSG308");
                            return false;
                        }
                        //重複チェック
                        $db = Query::dbCheckOut();
                        $ret_val = $db->getOne(knjb0031Query::countChair($this));
                        if ($this->cmd == "insert" && $ret_val > 0) {
                            //群コードチェック---2004.04.25
                            $ret_val2 = $db->getOne(knjb0031Query::checktoGroupcd($this));
                            if ($ret_val2 > 0) $group = " 教科に存在しています。";
                            else               $group = " 群に存在しています。";
                            $this->setWarning("MSG302", "(講座コード)".$group);
                            return false;
                        }
                        Query::dbCheckIn($db);
                        //存在チェック
                        if ($this->cmd == "update" && $ret_val == 0) {
                            $this->setWarning("MSG303", "(講座コード)");
                            return false;
                        }
            ***/
            //重複チェック
            $db = Query::dbCheckOut();
            $ret_val = $db->getOne(knjb0031Query::countChair($this, $g));
            //追加する（講座コードあり、更新日付なし）の場合、既に講座があれば重複エラーとする
            if (strlen($this->fields["CHAIRCD"][$g]) && !strlen($this->fields["UPDATED"][$g]) && $ret_val > 0) {
                $this->setWarning("MSG302", "講座コード：".$this->fields["CHAIRCD"][$g]);
                return false;
            }
            Query::dbCheckIn($db);
        }//for
        return true;
    }

    //追加および更新
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $result = knjb0031Query::getUpdateQuery($this);
            $this->setMessage("MSG201");
            unset($this->kensuu);
        }
    }
    //DELETE
    public function &getDeleteModel()
    {
        for ($g = 0; $g < $this->data_cnt; $g++) {
            //削除チェックＯＮのみ処理する
            if ($this->fields["DEL_FLG"][$g] != "1") {
                continue;
            }
            //キーの更新チェック
            if (!strlen($this->fields["CHAIRCD"][$g])) {
                $this->setWarning("MSG301", "(講座コード)");
                return false;
            }
            if (!strlen($this->fields["UPDATED"][$g])) {
                $this->warning = "保存されていないデータは削除できません。\\n" . "講座コード：".$this->fields["CHAIRCD"][$g];
                return false;
            }
        }//for
        $ret_val = knjb0031Query::getDeleteQuery($this);
        $this->message = "データを削除しました。";
        unset($this->kensuu);
    }


    // HRクラスの自動名簿生成
    public function getDownloadModel2()
    {
        // ＣＳＶ項目
        $header = array("YEAR"             => "※年度",
                        "SEMESTER"         => "※学期",
                        "CHAIRCD"          => "※講座コード",
                        "GRADE"            => "学年",
                        "HR_CLASS"         => "組",
                        "ATTENDNO"         => "出席番号",
                        "NAME"             => "氏名",
                        "SCHREGNO"         => "※学籍番号",
                        "APPDATE"          => "※適用開始日付",
                        "APPENDDATE"       => "※適用終了日付",
                        "ROW"              => "座席行",
                        "COLUMN"           => "座席列",
                        $this->lastColumn  => $this->lastColumn);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $header_data = false;
        $contents  = "";
        $contents .= implode($header, ",")."\n";

        $year = substr($this->term, 0, 4);
        $semester = substr($this->term, 5);

        $query = knjb0031Query::selectAutoChairStd($this, $year, $semester);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            // DB追加
            $query = knjb0031Query::insertAutoChairStd($row, $year, $semester, STAFFCD);
            $db->query($query);
            // CSV出力
            $row = str_replace(",", ".", $row); // データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $contents .= $year .",";
            $contents .= $semester .",";
            $contents .= $row["CHAIRCD"] .",";
            $contents .= $row["GRADE"] .",";
            $contents .= $row["HR_CLASS"] .",";
            $contents .= $row["ATTENDNO"] .",";
            $contents .= $row["NAME"] .",";
            $contents .= $row["SCHREGNO"] .",";
            $contents .= $row["SDATE"] .",";
            $contents .= $row["EDATE"] .",";
            $contents .= "" .",";
            $contents .= "" .",";
            $contents .= $this->lastColumn."\n";
            $header_data = true;
        }
        $result->free();

        $db->commit();
        Query::dbCheckIn($db);

        // ＣＳＶ出力
        if ($header_data) {
            if (!$this->Properties["useXLS"]) {
                $filename = sprintf("%s.csv", "講座受講生徒データ(自動生成)");
                common::downloadContents($contents, $filename);
                return true;
            } else {
                $this->message = "データを更新しました。";
                return false;
            }
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}

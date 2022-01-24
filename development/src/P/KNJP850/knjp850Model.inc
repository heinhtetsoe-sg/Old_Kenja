<?php

require_once('for_php7.php');

class knjp850Model extends Model
{
    public $field        = array();
    public $cmd;
    public $schregno;
    public $name;          //生徒氏名

    public function knjp850Model()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->schregno);
            unset($this->name);
            unset($this->schoolKind);
            unset($this->sdate);
            unset($this->seq);
            unset($this->field);
            unset($this->optContactDiv);
            unset($this->optSort);
            unset($this->csvSchregnoList);
            unset($this->sendSchregno);
        }

        //親画面から呼び出されたときの生徒検索画面の条件
        if (VARS::get("SEND_SCHREGNO") != "") {
            $this->sendSchregno  = VARS::get("SEND_SCHREGNO");
        }

        $this->year = CTRL_YEAR;
        $this->semester = CTRL_SEMESTER;

        //入力条件
        if (VARS::post("OPT_CONTACT_DIV") != "") {
            //カテゴリ選択
            $this->optContactDiv = VARS::post("OPT_CONTACT_DIV");
        }
        
        if (VARS::post("OPT_SORT") != "") {
            //表示順
            $this->optSort = VARS::post("OPT_SORT");
        }

        //編集画面
        $this->field = array("CONTACT_SDATE"    =>  str_replace("/", "-", VARS::post("CONTACT_SDATE")),   //対応日付
                             "SEQ"              =>  trim(VARS::post("SEQ")),       //通番
                             "CONTACT_DIV"      =>  VARS::post("CONTACT_DIV"),     //カテゴリ
                             "STAFFCD"          =>  VARS::post("STAFFCD"),         //記録者
                             "CONTACT_REMARK"   =>  VARS::post("CONTACT_REMARK"),  //記録内容
                             "CONTACT_EDATE"    =>  str_replace("/", "-", VARS::post("CONTACT_EDATE")));  //対応完了日

        //記録内容設定
        $this->contactRemark_moji = "40";
        $this->contactRemark_gyou = "3";

        //ゼロ埋め
        if ($this->field["SEQ"] != "") {
            $this->field["SEQ"] = sprintf("%03d", $this->field["SEQ"]);
        }
        $this->cmd = VARS::request("cmd");

        if (VARS::get("SCHREGNO") != "") {
            $this->schregno    = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME") != "") {
            $this->name    = VARS::get("NAME");
        }
        if (VARS::get("SCHOOL_KIND") != "") {
            $this->schoolKind  = VARS::get("SCHOOL_KIND");
        }
        if (VARS::get("CONTACT_SDATE") != "") {
            $this->sdate  = VARS::get("CONTACT_SDATE");
        }
        if (VARS::get("SEQ") != "") {
            $this->seq = VARS::get("SEQ");
        }

        //一括CSV対象者リスト
        $this->csvSchregnoList = array();
        if ($this->cmd == "csv") {
            $this->csvSchregnoList[0] = $this->schregno;
        } elseif ($this->cmd == "csv2" && VARS::post("IKKATU_LIST") != "") {
            $this->csvSchregnoList = explode(",", VARS::post("IKKATU_LIST"));
        }

        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    public function validateInput($kind)
    {
        if ($this->schregno=="") {
            $this->setWarning("MSG304", "(左より生徒を選択してから行ってください)");
            return false;
        }

        //リスト選択チェック
        if ($kind == "update" || $kind == "delete") {
            if ($this->sdate == "" || $this->sdate != $this->field["CONTACT_SDATE"]) {
                $this->setWarning("MSG308", "( 対応日付 )");
                return false;
            } elseif ($this->seq == "" || $this->seq != $this->field["SEQ"]) {
                $this->setWarning("MSG308", "( 通番 )");
                return false;
            }
        }

        //必須入力
        if ($this->field["CONTACT_SDATE"] == "") {
            if ($kind == "insert") {
                $this->setWarning("MSG301", "( 対応日付 )");
                return false;
            } else {
                $this->setWarning("MSG308", "( 対応日付 )");
                return false;
            }
        }
        if ($this->field["SEQ"] == "") {
            if ($kind == "insert") {
                //INSERT時に未入力の場合は自動採番なのでスルー
            } else {
                $this->setWarning("MSG308", "( 通番 )");
                return false;
            }
        }

        if ($kind != "delete") {
            if ($this->field["CONTACT_DIV"] == "") {
                $this->setWarning("MSG301", "( カテゴリ )");
                return false;
            } elseif ($this->field["STAFFCD"] == "") {
                $this->setWarning("MSG301", "( 記録者 )");
                return false;
            } elseif (trim($this->field["CONTACT_REMARK"]) == "") {
                $this->setWarning("MSG301", "( 記録内容 )");
                return false;
            }
        }


        if ($kind == "insert" || $kind == "update") {
            //サイズチェック
            if ($this->validate_row_cnt($this->field["CONTACT_REMARK"], ($this->contactRemark_moji * 2)) > $this->contactRemark_gyou) {
                $this->setWarning("MSG915", "(記録内容は{$this->contactRemark_gyou}行までです)");
                return false;
            }
            //通番チェック
            if ($this->field["SEQ"] != "") {
                $seqNum = intval($this->field["SEQ"]);
                if ($seqNum < 1 || $seqNum > 999) {
                    $this->setWarning("MSG914", "通番は001～999の範囲までです。");
                    return false;
                }
            }
        }

        //重複チェック
        if ($kind == "insert") {
            $db = Query::dbCheckOut();
            $existsCnt = knjp850Query::getExistsCnt($db, $this);
            Query::dbCheckIn($db);

            if ($existsCnt > 0) {
                $this->setWarning("MSG918");
                return false;
            }
        }

        return true;
    }

    public function &getUpdateModel()
    {
        if ($this->validateInput("update")) {
            $this->update();
        }
    }

    public function &getInsertModel()
    {
        if ($this->validateInput("insert")) {
            $this->insert();
            $this->sdate   = $this->field["CONTACT_SDATE"];
            $this->seq     = $this->field["SEQ"];
        }
    }

    public function &getDeleteModel()
    {
        if ($this->validateInput("delete")) {
            $this->delete();
            $this->field   = array();
            $this->sdate   = "";
            $this->seq     = "";
        }
    }

    public function &delete()
    {
        $db = Query::dbCheckOut();

        knjp850Query::getDeleteQuery($db, $this);
        $this->setMessage("MSG201");

        Query::dbCheckIn($db);
    }

    //追加
    public function &insert()
    {
        $db = Query::dbCheckOut();

        //未入力の場合は採番
        if ($this->field["SEQ"] == "") {
            $maxSeq = knjp850Query::getMaxSeq($db, $this);
            $nextSeq = ($maxSeq != "") ? intval($maxSeq) + 1 : 1;
            if ($nextSeq > 999) {
                $this->setWarning("MSG915", "通番が999を超えたため、自動採番に失敗しました。");
                return false;
            }
            $this->field["SEQ"] = sprintf("%03d", $nextSeq);
        }
        knjp850Query::getInsertQuery($db, $this);
        $this->setMessage("MSG201");

        Query::dbCheckIn($db);
    }

    //更新
    public function &update()
    {
        $db = Query::dbCheckOut();

        knjp850Query::getUpdateQuery($db, $this);
        $this->setMessage("MSG201");

        Query::dbCheckIn($db);
    }


    /********************/
    /*  ＣＳＶ出力処理  */
    /********************/

    public function getDownloadModel()
    {
        $db = Query::dbCheckOut();

        /****************/
        /*  ヘッダー例  */
        /****************/

        $header = array();
        $header["SCHREGNO"]         = "学籍番号";
        $header["SCHOOL_KIND_NAME"] = "学校区分";
        $header["GNAME"]            = "学年";
        $header["HNAME"]            = "組";
        $header["ATTENDNO"]         = "出席番号";
        $header["NAME"]             = "生徒氏名";
        $header["NAME_KANA"]        = "生徒氏名かな";
        $header["GRD_DATE"]         = "転退学日";
        $header["CONTACT_SDATE"]    = "対応日";
        $header["CONTACT_EDATE"]    = "対応完了日";
        $header["SEQ"]              = "通番";
        $header["STAFFCD"]          = "記録者コード";
        $header["STAFFNAME_SHOW"]   = "記録者";
        $header["CONTACT_DIV"]      = "カテゴリコード";
        $header["CONTACT_DIV_NAME"] = "カテゴリ名";
        $header["CONTACT_REMARK"]   = "記録内容";

        /**********************/
        /*  ヘッダー部分出力  */
        /**********************/
        $contents  = "";        //クリア
        $header_data = false;   //出力フラグ

        $contents .= implode($header, ",")."\n";

        /********************/
        /*  データ部分出力  */
        /********************/
        $query = "";
        $query = knjp850Query::selectCsvMainQuery($this);

        //データ整形
        $preprocessFunc = function (&$row) {
            //年組取得(桐蔭はHR_NAMEの形式は学年-組なのでハイフン区切りで取得)
            $tmp = explode("-", $row["HR_NAME"]);
            $row["GNAME"] = $tmp[0] ?: "";
            $row["HNAME"] = $tmp[1] ?: "";

            //日付変換
            $date_array = array("GRD_DATE", "CONTACT_SDATE", "CONTACT_EDATE");
            foreach ($date_array as $dateKey) {
                $row[$dateKey] = str_replace("-", "/", $row[$dateKey]);
            }

            //ダブルクォート
            $row["CONTACT_REMARK"] = "\"".$row["CONTACT_REMARK"]."\"";
        };

        //SQL文発行
        if ($query) {
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //カンマ変換
                $row = str_replace(",", ".", $row);
                $row = preg_replace("/\r\n|\r|\n/", "\n", $row);
                $array = $header;

                //前処理
                $preprocessFunc($row);

                //ヘッダ順に値をセット
                $sep = "";
                foreach ($array as $field => $label) {
                    $contents .= $sep.$row[$field];
                    $sep = ",";
                }
                $contents .= "\n";

                $header_data = true;
            }
            $result->free();
        }

        /**************************/
        /*  ファイル名用名称取得  */
        /**************************/

        //ファイル名
        $date = date("Ymd_His");
        $title = $this->year.'年度_保護者連絡履歴管理_'.$date;

        Query::dbCheckIn($db);
        //ＣＳＶ出力
        if ($header_data) {
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}

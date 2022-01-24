<?php

require_once('for_php7.php');

class knjb1256Model extends Model {
    var $semester;
    var $grade;
    var $selectdata;
    var $selectdata2;
    var $cmd;

    function knjb1256Model() {
    }

    function init() {
        $this->cmd = VARS::request("cmd");

        $this->field = array("YEAR"         =>  VARS::post("YEAR"),
                             "RIREKI_CODE"  =>  VARS::post("RIREKI_CODE"),
                             "RIREKI_CODE_COPY"  =>  VARS::post("RIREKI_CODE_COPY"),
                             "COURSECODE"   =>  VARS::post("COURSECODE"),
                             "PATTERN_CD"   =>  VARS::post("PATTERN_CD"),
                             "HR_CLASS"     =>  VARS::post("HR_CLASS")
                            );

        unset($this->selectStd);

        if (VARS::post("selectStd") != NULL) {
            $this->selectStd = explode(',', VARS::post("selectStd"));
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    function validateInput()
    {
        return true;
    }

    //更新用のモデルを返します
    function &getUpdateModel() {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //入力チェック
        if ($this->validateInput()) {
            knjb1256Query::getUpdateQuery($db, $this);
        }

        $query = knjb1256Query::getSeme($this);
        $result = $db->query($query);
        $semeArray = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $semeArray[] = $row;
        }
        $result->free();


        //生徒データをInsert
        if (get_count($this->selectStd) > 0) {
            list($coursecd, $majorcd, $coursecode) = preg_split("/-/", $this->field["COURSECODE"]);
            foreach ($this->selectStd as $schregNo) {
                $query = knjb1256Query::deleteSubclassStd($this, $schregNo);
                $db->query($query);

                //履修登録
                $query = knjb1256Query::getSubclassCd($this, $schregNo);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    foreach ($semeArray as $key => $semeData) {
                        $regdCntSql = knjb1256Query::getRegdCnt($this, $semeData, $schregNo);
                        $regdCnt = $db->getOne($regdCntSql);
                        if ($regdCnt < 1) {
                            continue;
                        }
                        $query = knjb1256Query::insertSubclassStd($db, $this, $semeData, $schregNo, $row);
                        if ($query) {
                            $db->query($query);
                        }
                    }
                }
                $result->free();

            }
        }

        $this->setMessage("MSG201");

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

    //コピー項目チェック
    function CopyCheck($db)
    {
        //コピー先の対象データのカウント
        $getCount = $db->getOne(knjb1256Query::getCountStdData($this));
        if ($getCount > 0) {
            $this->setWarning("MSG203", "コピー先の履修登録日には既にデータが存在します。");
            return false;
        }
        return true;
    }

    //コピー用のモデルを返します
    function &getCopyModel() {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //コピー先チェック
        if ($this->CopyCheck($db)) {
        
            //COMP_CREDITS_PATTERN_STD_COURSE_DATのコピー
            $query = knjb1256Query::getCopyCompQuery($this);
            $db->query($query);
            
            //SUBCLASS_COMP_SELECT_MSTのコピー
            $query = knjb1256Query::getCopyRirekiQuery($this);
            $db->query($query);
            
        }
        $this->setMessage("MSG201");

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }
    
}
?>

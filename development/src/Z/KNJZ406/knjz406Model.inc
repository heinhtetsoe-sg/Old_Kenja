<?php

require_once('for_php7.php');

class knjz406Model extends Model {

    var $cmd;
    var $control_data = array();    //コントロールのデータを保持
    var $field = array();
    var $auth;                      //権限
    var $setupFlg;                  //編集フラグ 1:編集中
    var $keepUnitDatArray = array();
    var $updUnitDatArray = array();

    function init() {
        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if (VARS::get("PROGRAMID")) {
            common::GetControlMaster_Fnc($this->control_data);
            $this->field = array();
            unset($this->auth);
            unset($this->setupFlg);
        }

        $this->cmd = VARS::request("cmd");
        $this->auth = AUTHORITY;
        $this->setupFlg = VARS::post("setupFlg");

        $this->field["DATA_DIV"]         = VARS::post("DATA_DIV");
        $this->field["GRADE_HR_CLASS"]   = VARS::post("GRADE_HR_CLASS");
        $this->field["SUBCLASSCD"]       = VARS::post("SUBCLASSCD");
        $this->kyoukaTantouList          = (VARS::post("kyoukaTantouList")) ? explode(",", VARS::post("kyoukaTantouList")) : array();
    }

    /********/
    /* 配列 */
    /********/
    function setDataArray() {
        //TEXT(画面データ)
        //DB(保持データ)
        $setArray = array("UNIT_L_NAME"     => "TEXT",
                          "SEQ"             => "DB",
                          "SEMESTER"        => "DB",
                          "UNIT_TEST_DATE"  => "TEXT"
                          );
        //初期化
        $maxCnt = get_count($this->keepUnitDatArray) - 1;//最終行
        $moveDownFlg = false;
        $setCnt = 0;
        $this->updUnitDatArray = array();
        foreach ($this->keepUnitDatArray as $key => $row) {
            $isCheck = strlen(VARS::post("RANK".$key)); //選択(指定行)・・・複数選択は不可。
            //削除・・・指定行を削除。
            if ($this->cmd == "del" && $isCheck) {
                continue;
            }
            //挿入・・・指定行の上に空白行を追加。
            if ($this->cmd == "ins" && $isCheck) {
                foreach ($setArray as $name => $type) {
                    $this->updUnitDatArray[$setCnt][$name] = "";
                }
                $this->updUnitDatArray[$setCnt]["RANK"] = $setCnt;
                $setCnt++;
            }
            //通常
            foreach ($setArray as $name => $type) {
                $value = ($type == "TEXT") ? VARS::post($name.$key) : $row[$name];
                if ($name == "SEMESTER") {
                    $postDate = VARS::post("UNIT_TEST_DATE".$key);
                    $setSemester = "";
                    if ($postDate) {
                        $setSemester = knjz406Query::getSemester(VARS::post("UNIT_TEST_DATE".$key));
                    }
                    $this->updUnitDatArray[$setCnt][$name] = strlen($setSemester) ? $setSemester : "";
                } else {
                    $this->updUnitDatArray[$setCnt][$name] = strlen($value) ? $value : "";
                }
            }
            //延長・・・指定行の下に同一行を追加。つまり、指定行のコピー。
            if ($this->cmd == "extend" && $isCheck) {
                $setCnt++;
                $this->updUnitDatArray[$setCnt] = $this->updUnitDatArray[$setCnt-1];
                $this->updUnitDatArray[$setCnt]["RANK"] = $setCnt;
                $this->updUnitDatArray[$setCnt]["SEQ"] = "";
            }
            //上へ移動・・・指定行を１行上へ移動。
            if ($this->cmd == "moveUp" && $isCheck && $setCnt != 0) {
                $moveUpArray = array();
                $moveUpArray[$setCnt] = $this->updUnitDatArray[$setCnt-1];
                $moveUpArray[$setCnt-1] = $this->updUnitDatArray[$setCnt];
                $this->updUnitDatArray[$setCnt] = $moveUpArray[$setCnt];
                $this->updUnitDatArray[$setCnt-1] = $moveUpArray[$setCnt-1];
                $this->updUnitDatArray[$setCnt-1]["RANK"] = $setCnt-1;
            }
            //下へ移動・・・指定行を１行下へ移動。
            if ($moveDownFlg) {
                $moveDownArray = array();
                $moveDownArray[$setCnt] = $this->updUnitDatArray[$setCnt-1];
                $moveDownArray[$setCnt-1] = $this->updUnitDatArray[$setCnt];
                $this->updUnitDatArray[$setCnt] = $moveDownArray[$setCnt];
                $this->updUnitDatArray[$setCnt-1] = $moveDownArray[$setCnt-1];
                $this->updUnitDatArray[$setCnt]["RANK"] = $setCnt;
                $moveDownFlg = false;
            }
            if ($this->cmd == "moveDown" && $isCheck && $setCnt != $maxCnt) {
                $moveDownFlg = true;
            }
            $setCnt++;
        }
        //追加・・・最終行に空白行を追加。
        if ($this->cmd == "add") {
            foreach ($setArray as $name => $type) {
                $this->updUnitDatArray[$setCnt][$name] = "";
            }
            $this->updUnitDatArray[$setCnt]["RANK"] = $setCnt;
        }
//echo var_dump($this->keepUnitDatArray);
//echo var_dump($this->updUnitDatArray);
    }

    /* 入力項目チェック */
    function validateInput() {
        return true;
    }

    /* 更新処理 */
    function &getUpdateModel() {
        if ($this->validateInput()){
            $this->update();
            $this->setMessage("MSG201");
            $this->setupFlg = ""; //編集中フラグOFF
        }
        return;
    }

    /* 編集対象データを更新 */
    function &update() {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        knjz406Query::getUpdUnitDat($db, $this);

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }
}
?>

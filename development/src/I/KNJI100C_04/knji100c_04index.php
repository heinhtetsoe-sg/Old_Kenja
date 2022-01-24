<?php

require_once('for_php7.php');

require_once('knji100c_04Model.inc');
require_once('knji100c_04Query.inc');
//区分に関しての出力設定
define("OUT_CODE_NAME", 1);
define("OUT_CODE_ONLY", 2);
define("OUT_NAME_ONLY", 3);

class knji100c_04Controller extends Controller
{
    public $ModelClassName = "knji100c_04Model";
    public $ProgramID      = "KNJI100C";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "edit":
                case "changeGrade":
                    $this->callView("knji100c_04Form1");
                    break 2;
                case "csv":
                    if (!$sessionInstance->getCsvModel()) {
                        //変更済みの場合は詳細画面に戻る
                        $sessionInstance->setCmd("edit");
                        break 1;
                    }
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knji100c_04Ctl = new knji100c_04Controller();

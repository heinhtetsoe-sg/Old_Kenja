<?php

require_once('for_php7.php');
require_once('knjh400_TyousasyoSyokenModel.inc');
require_once('knjh400_TyousasyoSyokenQuery.inc');

class knjh400_TyousasyoSyokenController extends Controller
{
    public $ModelClassName = "knjh400_TyousasyoSyokenModel";
    public $ProgramID      = "KNJH400";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "reload3":     //RECORD_TOTALSTUDYTIME_DAT (通知書) より読込む
                case "updEdit":
                case "edit":
                case "sogo_yomikomi":
                case "select_pattern":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh400_TyousasyoSyokenForm1");
                    break 2;
                case "reload2_ok":  //学習指導要録より読込(OK)
                case "reload2_cancel":  //学習指導要録より読込(キャンセル)
                case "reload4":     //通知書より読込
                case "torikomi3":
                case "torikomi4":
                case "form2_first": //「出欠の～」の最初の呼出
                case "form2":       //出欠の～
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh400_TyousasyoSyokenForm2");
                    break 2;
                case "formSeiseki_first": //「成績参照」の最初の呼出
                case "formSeiseki":       //「成績参照」
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh400_TyousasyoSyokenSubFormSeiseki");
                    break 2;
                case "formYorokuSanshou_first": //「指導要録参照」の最初の呼出
                case "formYorokuSanshou":       //「指導要録参照」
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh400_TyousasyoSyokenSubFormYorokuSanshou");
                    break 2;
                case "formYorokuSanshou2_first": //「指導要録参照」の最初の呼出
                case "formYorokuSanshou2":       //「指導要録参照」
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh400_TyousasyoSyokenSubFormYorokuSanshou2");
                    break 2;
                case "formShojikouTori":     //「指導上参考となる諸事項選択」
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh400_TyousasyoSyokenSubFormShojikouTori");
                    break 2;
                case "formBikouTori":     //「備考」
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh400_TyousasyoSyokenSubFormBikouTori");
                    break 2;
                case "replace":
                    $this->callView("knjh400_TyousasyoSyokenSubForm3"); //一括更新画面
                    break 2;
                case "formYourokuIkkatsuTorikomi_first":
                case "formYourokuIkkatsuTorikomi":
                    $this->callView("knjh400_TyousasyoSyokenSubFormYourokuIkkatsuTorikomi"); //指導要録所見一括取込画面
                    break 2;
                case "reset":
                    $this->callView("knjh400_TyousasyoSyokenForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh400_TyousasyoSyokenForm1");
                    break 2;
                case "back":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh400_TyousasyoSyokenForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh400_TyousasyoSyokenCtl = new knjh400_TyousasyoSyokenController();
//var_dump($_REQUEST);
